# lecture
강좌 신청 웹 어플리케이션 백엔드 개발

- Java 17 
- Spring Boot 3.3.3
- Spring Data JPA
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
- Users 테이블은 사번 관련 정보를 저장하며, 사번만 유일 식별자로 관리하여 사번 관련 처리가 쉽도록 설계했습니다.

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
- 트랜잭션 처리 : 강연 신청 시 LectureStats에서 잔여 좌석 감소와 예약 정보 등록이 동시에 이루어져야 합니다. 이를 위해 트랜잭션 관리를 통해 좌석 감소와 예약 등록이 원자적으로 처리되도록 해야 합니다. 강연이 취소되거나 수정될 때 관련된 좌석 정보와 예약 정보도 함께 처리해야 합니다.
- 비관적 락: 강연 신청 시 LectureStats 에서 잔여 좌석 조회 후 잔여 좌석 감소와 예약 좌석 증가를 동시에 여러 쓰레드에서 처리함으로 동시성 문제가 발생합니다. 잔여 좌석을 동시에 두 쓰레드가 조회 후 데이터를 중복 업데이트하는 현상이 발생할 수 있습니다. 이를 해결하기 위해 NamedLock 방법으로 처리하려 하였으나 H2 Database에서 이를 지원하지 않아 비관적 락 방법을 사용하였습니다. 비관적 락은 DB data row에 락을 걸어 순차적으로 처리하는 방식입니다. 이 방법은 성능 부하가 많고 데이터에 직접 락을 걸기 때문에 수강 신청 이외의 서비스에서 데이터 조회하는 것에도 불필요하게 락이 걸립니다. NamedLock 방법은 데이터에 직접 락을 거는 방식이 아니라 리소스 이름으로 락을 생성하는 방식입니다. 수강 신청 시에만 락을 걸어 순차적으로 처리할 수 있습니다. 최근에는 분산 환경에서 동시성 문제를 해결하기 위해 Redis나 kafka와 같은 서버를 이용한 방식을 많이 사용하고 있습니다. 

# 기타 고려 사항
### Popularity_score 칼럼을 최근 3일간의 강연 신청 수로 업데이트: 실시간 업데이트 방식 (이벤트 기반)

#### 장점:
- **실시간 통계 반영**: 강연 신청이 발생할 때마다 즉시 `popularity_score`가 업데이트되므로, **실시간 인기 강연**을 관리하는 데 매우 유리합니다.
- **즉시성**: 별도의 배치 작업 없이 실시간으로 데이터가 반영되므로, 사용자에게 최신 정보를 제공합니다.

#### 단점:
- **성능 저하 가능성**: 강연 신청이 발생할 때마다 **추가적인 데이터베이스 연산**이 발생합니다. 많은 신청이 동시에 들어오는 경우 **성능 저하**가 발생할 수 있습니다.
- **병목 현상**: 특히 **대규모 트래픽**에서, 동시에 여러 강연 신청이 몰리는 경우 **DB 연산**이 병목을 일으킬 가능성이 큽니다. 이로 인해 **응답 시간 지연** 또는 시스템 부하가 발생할 수 있습니다.

# 테스트

### 단위테스트 (Junit)
com.kidari.lecture.LectureApplicationTests.java
BackOffice API
1. 강연 목록 조회 테스트
	@Test
	public void testGetAllLectures()
2. 강연 등록 테스트
	@Test
	public void testCreateLecture() 
3. 강연 신청자 목록 조회 테스트
	@Test
	public void testGetLectureUsers()
Front API
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

com.kidari.lecture.ReservationTest.java
동시성 테스트(h2 db에서 잘 작동하지 않음..)
1. 100 명의 사용자가 강연 신청
    @Test
    public void reserve_100_user() throws InterruptedException
   
### Swagger(http://localhost:8080/swagger-ui/index.html)
