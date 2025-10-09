package com.insp.poc_login.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@Entity
@Table(name = "login_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserLogin {
    @Id
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    @Column
    private boolean enabled = true;

    @Column(name = "forgot_pwd_timestamp")
    private LocalDateTime forgotPwdTimestamp;
}
