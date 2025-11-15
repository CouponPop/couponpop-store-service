import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    vus: 200,             
    duration: '5m',      
    thresholds: {
        http_req_failed: ['rate<0.01'],   
        http_req_duration: ['p(95)<500'], 
    }
};

const keywords = [
    '커피', '카페', '음식', '식당', '편의점',
    '디저트', '베이커리', '브런치', '술집', '술', 
    '김밥', '삼겹살', '치킨', '피자', '햄버거',
    '좋은 분위기', '맛있는 커피'
];

export default function () {
    const keyword = keywords[Math.floor(Math.random() * keywords.length)];

    const searchUrl = `http://host.docker.internal:8080/api/v1/stores/search/keyword?keyword=${encodeURIComponent(keyword)}`;

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
        tags: { name: 'StoreSearch_Keyword' },
    };

    const res = http.get(searchUrl, params);

    check(res, {
        'Search: status 200': (r) => r.status === 200,
        'Search: body not empty': (r) => r.body && r.body.length > 0,
    });

    sleep(0.5);
}