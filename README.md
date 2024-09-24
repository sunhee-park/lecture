# lecture
강좌 신청 웹 어플리케이션 백엔드 개발

- Java 17 
- Spring Boot 3.3.3
- Spring Data JPA
- QueryDSL
- H2 Database
- Gradle

# 주요 테이블 목록
- **Lecture (강연 테이블)**: 강연 관련 기본 정보를 저장합니다.
- **LectureStats (강연 통계 테이블)**: 강연의 좌석 및 통계 데이터를 관리합니다.
- **Reservation (예약 테이블)**: 사번을 통한 강연 신청 정보를 저장합니다.
- **Users (사번 테이블)**: 사번과 관련된 기본 정보를 저장합니다.

### 1. Lecture 테이블
강연 정보를 담고 있으며, 좌석 및 강연 관련 내용도 함께 관리합니다.

| 컬럼명      | 데이터 타입  | 설명                |
|------------|--------------|---------------------|
| lecture_id | BIGINT       | 강연 고유 ID (PK)   |
| title      | VARCHAR(255) | 강연 제목           |
| lecturer   | VARCHAR(255) | 강연자              |
| venue      | VARCHAR(255) | 강연 장소           |
| capacity   | INT          | 강연 최대 좌석 수   |
| start_time | DATETIME     | 강연 시작 시간      |
| end_time   | DATETIME     | 강연 종료 시간      |
| content    | TEXT         | 강연 내용           |

### 2. LectureStats 테이블
강연과 연결된 통계 정보와 잔여 좌석 수를 관리합니다.

| 컬럼명          | 데이터 타입 | 설명                                  |
|-----------------|-------------|---------------------------------------|
| stats_id        | BIGINT      | 통계 고유 ID (PK)                    |
| lecture_id      | BIGINT      | 강연 ID (FK, Lecture 테이블 참조)     |
| available_seats | INT         | 현재 남은 좌석 수                    |
| reserved_seats  | INT         | 예약된 좌석 수                       |
| popularity_score| INT         | 인기 점수 (최근 3일간 신청 수)       |

Lecture 테이블과 1:1 관계를 형성합니다. LectureStats는 잔여 좌석 및 예약된 좌석을 관리하며, 인기 강연 정보를 빠르게 조회할 수 있는 구조를 제공합니다.

### 3. Reservation 테이블
강연 신청 정보를 담고 있습니다. 사번을 통해 강연을 신청할 수 있으며, 강연 ID와 사번을 통해 유일성을 보장합니다.

| 컬럼명          | 데이터 타입  | 설명                                  |
|-----------------|--------------|---------------------------------------|
| reservation_id  | BIGINT       | 예약 고유 ID (PK)                    |
| employee_number | VARCHAR(5)   | 신청자 사번                          |
| lecture_id      | BIGINT       | 신청한 강연 ID (FK, Lecture 테이블 참조) |
| reservation_time| DATETIME     | 예약한 시간                          |

`lecture_id`와 `employee_number`로 강연 중복 신청을 방지합니다.

### 4. Users 테이블
사번 정보를 관리합니다. 사번에 대한 기본 정보를 저장하지만, 간단하게 사번만 있을 수 있습니다.

| 컬럼명          | 데이터 타입  | 설명        |
|-----------------|--------------|-------------|
| employee_number | VARCHAR(5)   | 사번 (PK)   |
| name            | VARCHAR(255) | 이름        |

### ERD 설명
- Lecture와 LectureStats는 1:1 관계로, 강연 정보를 기준으로 좌석 및 인기 통계를 따로 관리하여 강연 정보와 좌석 통계의 독립성을 유지했습니다.
- Reservation 테이블은 강연 신청을 관리하며, 사번과 강연 ID를 통해 중복 신청을 방지합니다.
- Employee 테이블은 사번 관련 정보를 저장하며, 사번만 유일 식별자로 관리하여 사번 관련 처리가 쉽도록 설계했습니다.

# Package structure
src/main/java/com/kidari/lecture  
├── config  
├── controller  
├── dto  
├── entity  
├── exception  
├── repository  
├── service  
├── util  
└── LectureApplication.java

# API 설계
### BackOffice API
- 강연 목록 조회: /api/backoffice/lectures (GET)
전체 강연 목록을 조회
- 강연 등록: /api/backoffice/lectures (POST)
강연자, 강연장, 신청 인원, 강연 시간, 강연 내용을 입력
- 강연 신청자 목록 조회: /api/backoffice/lectures/{lecture_id}/users (GET)
특정 강연에 대한 신청자 사번 목록 조회

### Front API
- 강연 목록 조회: /api/front/lectures (GET)
신청 가능한 강연 목록 조회
강연 시작 1주일 전부터 노출, 시작 1일 후에 노출 중지
- 강연 신청: /api/front/lectures/{lecture_id}/reserve (POST)
사번을 입력하여 강연 신청
중복 신청 제한 로직 포함
- 신청 내역 조회: /api/front/reservations (GET)
사번을 입력하여 신청한 강연 목록 조회
- 신청한 강연 취소: /api/front/reservations/{reservation_id}/cancel (DELETE)
신청된 강연을 취소
- 실시간 인기 강연 조회: /api/front/lectures/popular (GET)
최근 3일간 가장 신청이 많은 강연 순으로 노출

# 동시성, 데이터 일관성
- 동시성 문제: 강연 신청 시 LectureStats에서 잔여 좌석 감소와 예약 정보 등록이 동시에 이루어져야 합니다. 이를 위해 트랜잭션 관리를 통해 좌석 감소와 예약 등록이 원자적으로 처리되도록 해야 합니다.
- 데이터 일관성 유지: 강연 정보 수정 시, LectureStats와 Reservation 간의 데이터 일관성이 유지되어야 합니다. 이를 위해 강연이 취소되거나 수정될 때 관련된 좌석 정보와 예약 정보도 함께 처리해야 합니다.


# 기타 고려 사항

- 실시간 인기 강연


- 잔여 좌석, 예약된 좌석 칼럼



# 테스트

### 단위테스트 (Junit)
com.kidari.lecture.LectureApplicationTests.java
1. 강연 목록 조회 테스트
	@Test
	public void testGetAllLectures()
2. 강연 등록 테스트
	@Test
	public void testCreateLecture() 
강연 등록 API를 호출하여 아래와 같은 JSON 데이터를 보낼 수 있습니다.
3. 강연 신청자 목록 조회 테스트
	@Test
	public void testGetLectureUsers()

1. 신청 가능한 강연 목록 조회 테스트
	@Test
	public void testGetAvailableLectures()
2. 강연 신청 테스트
	@Test
	public void testReserveLecture()
3. 신청 내역 조회 테스트
	@Test
	public void testGetReservations()
4. 신청한 강연 취소 테스트
	@Test
	public void testCancelReservation()
5. 실시간 인기 강연 테스트
	@Test
	public void testGetPopularLectures()

### 테스트(url 직접 호출)
POST /api/backoffice/lectures 요청

{
    "title": "Spring Boot 강연",
    "lecturer": "김개발",
    "venue": "서울 강남구 강연장",
    "capacity": 100,
    "startTime": "2024-10-01T10:00:00",
    "endTime": "2024-10-01T12:00:00",
    "content": "Spring Boot를 활용한 백엔드 개발에 대한 강연입니다."
}

응답
{
    "message": "강연이 성공적으로 등록되었습니다."
}


GET /api/backoffice/lectures/{lecture_id}/users 요청

GET /api/backoffice/lectures/1/users

응답

[
    "12345",
    "23456",
    "34567"
]

