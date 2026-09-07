import http from 'k6/http';
import { check } from 'k6';
import { Counter, Rate } from 'k6/metrics';

const successCount = new Counter('reservation_success');
const conflictCount = new Counter('reservation_conflict');
const unexpectedError = new Rate('unexpected_error');

const baseUrl = __ENV.BASE_URL || 'http://localhost:8080';
const tomorrow = new Date();
tomorrow.setDate(tomorrow.getDate() + 1);
const reservationDate = [
  tomorrow.getFullYear(),
  String(tomorrow.getMonth() + 1).padStart(2, '0'),
  String(tomorrow.getDate()).padStart(2, '0'),
].join('-');
const startAt = `${reservationDate}T10:00:00`;
const endAt = `${reservationDate}T11:00:00`;

export const options = {
  scenarios: {
    reservation_race: {
      executor: 'shared-iterations',
      vus: 100,
      iterations: 100,
      maxDuration: '1m',
    },
  },
  thresholds: {
    reservation_success: ['count==1'],
    reservation_conflict: ['count==99'],
    unexpected_error: ['rate==0'],
  },
};

// 이 테스트에서는 성공(201)과 이미 예약됨(409)을 모두 예상 응답으로 처리한다.
http.setResponseCallback(http.expectedStatuses(201, 409));

export default function () {
  const response = http.post(
    `${baseUrl}/api/v1/reservations`,
    JSON.stringify({
      studyRoomId: 1,
      guestName: `k6 사용자 ${__VU}`,
      guestEmail: `k6-${__VU}@example.com`,
      guestPhone: `010${String(__VU).padStart(8, '0')}`,
      startAt,
      endAt,
      purpose: 'k6 동시성 테스트',
    }),
    {
      headers: { 'Content-Type': 'application/json' },
    },
  );

  check(response, {
    '응답이 201 또는 409다': (res) => res.status === 201 || res.status === 409,
  });

  if (response.status === 201) {
    successCount.add(1);
  } else if (response.status === 409) {
    conflictCount.add(1);
  } else {
    unexpectedError.add(1);
  }
}
