package com.kidari.lecture.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReservationLectureDTO {

    private LectureDTO lecture;
    private  ReservationDTO reservation;

    public ReservationLectureDTO(LectureDTO lecture, ReservationDTO reservation) {
        this.lecture = lecture;
        this.reservation = reservation;
    }
}
