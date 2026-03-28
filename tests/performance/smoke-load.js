import http from 'k6/http';
import { check, sleep } from 'k6';

const baseUrl = __ENV.API_BASE_URL || 'http://127.0.0.1:8080';
const vus = Number(__ENV.K6_VUS || 20);
const duration = __ENV.K6_DURATION || '2m';

export const options = {
  vus,
  duration,
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<800'],
    checks: ['rate>0.95'],
  },
};

export default function () {
  const health = http.get(`${baseUrl}/actuator/health`);
  check(health, {
    'health is 200': (r) => r.status === 200,
  });

  const loginPayload = JSON.stringify({
    email: 'anna@example.com',
    password: 'password',
  });
  const login = http.post(`${baseUrl}/api/auth/login`, loginPayload, {
    headers: { 'Content-Type': 'application/json' },
  });
  check(login, {
    'login is 200': (r) => r.status === 200,
    'login returns token': (r) => {
      try {
        return Boolean(r.json('token'));
      } catch (e) {
        return false;
      }
    },
  });

  sleep(0.2);
}
