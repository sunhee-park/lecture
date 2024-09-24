package com.kidari.lecture;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.kidari.lecture.dto.LectureDTO;
import com.kidari.lecture.service.LectureService;
import com.kidari.lecture.repository.LectureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // 데이터베이스 롤백
public class LectureApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private LectureService lectureService;

	@Autowired
	private LectureRepository lectureRepository;

	@BeforeEach
	public void setup() {
		// 사전 데이터 설정
		// 강연 등록
		LectureDTO lectureDTO = new LectureDTO();
		lectureDTO.setLecturer("Kim Sunhee");
		lectureDTO.setTitle("SpringBoot");
		lectureDTO.setVenue("1 venue");
		lectureDTO.setCapacity(200);
		lectureDTO.setContent("Web Application Development with Spring Boot");

		LocalDateTime now = LocalDateTime.now();

		// 3일 더하기
		LocalDateTime startTime = now.plusDays(3);
		LocalDateTime endTime = startTime.plusDays(10);

		lectureDTO.setStartTime(startTime);
		lectureDTO.setEndTime(endTime);
		lectureService.createLecture(lectureDTO);

		// 강연 신청
		lectureService.reserveLecture(1l, "abc12");
		lectureService.reserveLecture(1l, "bbc02");
	}

	// 1. 강연 목록 조회 테스트
	@Test
	public void testGetAllLectures() throws Exception {
		MvcResult mvcResult = mockMvc.perform(get("/api/backoffice/lectures")
						.param("page", "0")
						.param("size", "10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray())
				.andReturn();

		// 응답의 내용을 포맷된 JSON 으로 출력
		String responseContent = mvcResult.getResponse().getContentAsString();
		printJson(responseContent);
	}

	// 2. 강연 등록 테스트
	@Test
	public void testCreateLecture() throws Exception {
		String lectureJson = "{"
				+ "\"title\":\"Python\","
				+ "\"lecturer\":\"Park Sunhee\","
				+ "\"venue\":\"2 venue\","
				+ "\"capacity\":100,"
				+ "\"startTime\":\"2024-09-27T08:00:00\","
				+ "\"endTime\":\"2024-09-27T10:00:00\","
				+ "\"content\":\"Web Application Development with Python\""
				+ "}";

		MvcResult mvcResult = mockMvc.perform(post("/api/backoffice/lectures")
						.contentType(MediaType.APPLICATION_JSON)
						.content(lectureJson))
				.andExpect(status().isOk())
				.andExpect(content().string("강연이 성공적으로 등록되었습니다."))
				.andReturn();

		// 응답의 내용을 포맷된 JSON 으로 출력
		String responseContent = mvcResult.getResponse().getContentAsString();
		System.out.println("응답 내용: " +responseContent);

		// 등록된 강연을 목록으로 조회
		MvcResult mvcResultList = mockMvc.perform(get("/api/backoffice/lectures")
						.param("page", "0")
						.param("size", "10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray())
				.andReturn();

		// 응답의 내용을 포맷된 JSON 으로 출력
		String responseContentList = mvcResultList.getResponse().getContentAsString();
		printJson(responseContentList);
	}

	// 3. 강연 신청자 목록 조회 테스트
	@Test
	public void testGetLectureUsers() throws Exception {
		Long lectureId = 1L;
		MvcResult mvcResult = mockMvc.perform(get("/api/backoffice/lectures/" + lectureId + "/users"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").isNumber())
				.andReturn();

		// 응답의 내용을 포맷된 JSON 으로 출력
		String responseContent = mvcResult.getResponse().getContentAsString();
		printJson(responseContent);
	}


	// 1. 신청 가능한 강연 목록 조회 테스트
	@Test
	public void testGetAvailableLectures() throws Exception {
		MvcResult mvcResult = mockMvc.perform(get("/api/front/lectures")
						.param("page", "0")
						.param("size", "10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray())
				.andReturn();

		// 응답의 내용을 포맷된 JSON 으로 출력
		String responseContent = mvcResult.getResponse().getContentAsString();
		printJson(responseContent);
	}

	// 2. 강연 신청 테스트
	@Test
	public void testReserveLecture() throws Exception {
		Long lectureId = 1L;
		String employeeNumber = "dev99";

		MvcResult mvcResult = mockMvc.perform(post("/api/front/lectures/" + lectureId + "/reserve")
						.param("employeeNumber", employeeNumber))
				.andExpect(status().isOk())
				.andExpect(content().string("강연 신청이 완료되었습니다."))
				.andReturn();

		// 응답의 내용을 포맷된 JSON 으로 출력
		String responseContent = mvcResult.getResponse().getContentAsString();
		System.out.println("응답 내용: " +responseContent);
	}

	// 3. 신청 내역 조회 테스트
	@Test
	public void testGetReservations() throws Exception {
		String employeeNumber = "abc12";

		MvcResult mvcResult = mockMvc.perform(get("/api/front/lectures/reservations")
						.param("employeeNumber", employeeNumber))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").isNumber())
				.andReturn();

		// 응답의 내용을 포맷된 JSON 으로 출력
		String responseContent = mvcResult.getResponse().getContentAsString();
		printJson(responseContent);
	}

	// 4. 신청한 강연 취소 테스트
	@Test
	public void testCancelReservation() throws Exception {
		Long reservationId = 1L;

		MvcResult mvcResult = mockMvc.perform(delete("/api/front/lectures/reservations/" + reservationId + "/cancel"))
				.andExpect(status().isOk())
				.andExpect(content().string("강연 신청이 취소되었습니다."))
				.andReturn();

		// 응답의 내용을 포맷된 JSON 으로 출력
		String responseContent = mvcResult.getResponse().getContentAsString();
		System.out.println("응답 내용: " +responseContent);
	}

	// 5. 실시간 인기 강연 테스트
	@Test
	public void testGetPopularLectures() throws Exception {
		MvcResult mvcResult = mockMvc.perform(get("/api/front/lectures/popular"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").isNumber())
				.andReturn();

		// 응답의 내용을 포맷된 JSON 으로 출력
		String responseContent = mvcResult.getResponse().getContentAsString();
		printJson(responseContent);
	}

	// JSON 문자열 출력
	private void printJson(String responseContent) throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		Object json = objectMapper.readValue(responseContent, Object.class);  // JSON 문자열을 Object로 변환
		ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();  // 포맷팅을 위한 ObjectWriter
		String prettyJson = writer.writeValueAsString(json);  // 포맷된 JSON 문자열
		System.out.println("응답 내용: " + prettyJson);
	}
}
