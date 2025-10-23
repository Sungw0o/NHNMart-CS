package com.nhnacademy.nhnmartcs.inquiry.domain;

import com.nhnacademy.nhnmartcs.user.domain.CSAdmin;
import lombok.Getter;
import lombok.Setter;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

@Getter
@Setter
public class Answer {

    private Long id;
    private String content;
    private LocalDateTime createdAt;

    private CSAdmin admin;

    public Answer(String content, CSAdmin admin) {
        this.content = content;
        this.admin = admin;
        this.createdAt = LocalDateTime.now();
    }
}
