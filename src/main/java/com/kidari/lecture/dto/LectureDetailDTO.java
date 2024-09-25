package com.kidari.lecture.dto;

import com.kidari.lecture.entity.Lecture;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class LectureDetailDTO {

    private LectureDTO lecture;

    // LectureStats 정보 추가
    private int availableSeats;
    private int reservedSeats;
    private int popularityScore;
}
