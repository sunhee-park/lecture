package com.kidari.lecture.controller;

import com.kidari.lecture.dto.LectureDTO;
import com.kidari.lecture.dto.LectureDetailDTO;
import com.kidari.lecture.service.LectureService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/backoffice/lectures")
public class BackOfficeController {

    @Autowired
    private LectureService lectureService;

    // 1. 강연 목록 조회
    @GetMapping
    public ResponseEntity<Page<LectureDetailDTO>> getAllLectures(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<LectureDetailDTO> lectures = lectureService.getAllLectures(page, size);
        return ResponseEntity.ok(lectures);
    }

    // 2. 강연 등록
    @PostMapping
    public ResponseEntity<String> createLecture(@RequestBody @Valid LectureDTO lectureDTO) {
        lectureService.createLecture(lectureDTO);
        return ResponseEntity.ok("강연이 성공적으로 등록되었습니다.");
    }

    // 3. 강연 신청자 목록(강연 별 신청한 사번 목록)
    @GetMapping("/{lectureId}/users")
    public ResponseEntity<List<String>> getLectureUsers(@PathVariable Long lectureId) {
        List<String> users = lectureService.getLectureUsers(lectureId);
        return ResponseEntity.ok(users);
    }


}
