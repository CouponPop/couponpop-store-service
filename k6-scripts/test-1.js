import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '30s', target: 50 },
        { duration: '1m', target: 200 },
        { duration: '30s', target: 0 },
    ],
    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<500'],
    },
    timeouts: {
        request: '120s',
    },
};

export default function () {
    const keyword = ['커피', '카페', '음식', '식당', '편의점'][Math.floor(Math.random() * 5)];
    const searchUrl = `http://host.docker.internal:8080/api/v1/stores/search/keyword?keyword=${encodeURIComponent(keyword)}`;

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
        tags: { name: 'Search' },
    };

    const res = http.get(searchUrl, params);

    check(res, {
        'Search: status 200': (r) => r.status === 200,
        'Search: has body': (r) => r.body && r.body.length > 0,
    });

    sleep(0.5);
}