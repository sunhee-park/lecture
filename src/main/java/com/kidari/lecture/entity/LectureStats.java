package com.kidari.lecture.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class LectureStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long statsId;

    @OneToOne
    @JoinColumn(name = "lecture_id")
    private Lecture lecture;

    private int availableSeats;

    private int reservedSeats;

    private int popularityScore;
}
