package com.kidari.lecture.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Lecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long lectureId;

    private String title;

    private String lecturer;

    private String venue;

    private int capacity;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String content;

    @OneToOne(mappedBy = "lecture", cascade = CascadeType.ALL)
    private LectureStats lectureStats;
}

