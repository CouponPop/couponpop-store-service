import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '2m', target: 50 },  
        { duration: '3m', target: 200 },  
        { duration: '2m', target: 500 },  
        { duration: '3m', target: 0 },    
    ],
    thresholds: {
        http_req_failed: ['rate<0.01'],  
        http_req_duration: ['p(95)<1000'] 
    }
};

const keywords = ['커피', '카페', '디저트', '베이커리', '브런치'];

export default function () {
    const keyword = keywords[Math.floor(Math.random() * keywords.length)];

    const url = `http://host.docker.internal:8080/api/v1/stores/search/keyword?keyword=${encodeURIComponent(keyword)}`;

    const res = http.get(url);

    check(res, {
        'status 200': (r) => r.status === 200,
        'body not empty': (r) => r.body && r.body.length > 0,
    });

    sleep(0.5);
}