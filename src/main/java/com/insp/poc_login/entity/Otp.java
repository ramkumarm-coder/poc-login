package com.insp.poc_login.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "otp")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Otp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String email;
    @Column
    private String code;
    @Column
    private LocalDateTime createdAt;
    @Column
    private boolean used = false;
}
