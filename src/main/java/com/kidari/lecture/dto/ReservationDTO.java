package com.kidari.lecture.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReservationDTO {
    private Long reservationId;
    private String employeeNumber;
    private Long lectureId;
    private String reservationTime;
}
