package com.kidari.lecture.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Users {

    @Id
    @Column(name = "employee_number", length = 5)
    private String employeeNumber;

    private String name;
}
