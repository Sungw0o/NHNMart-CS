package com.nhnacademy.nhnmartcs.inquiry.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class AnswerCreateRequest {

    @NotBlank(message = "답변 내용을 입력해주세요.")
    @Size(max = 40000, message = "답변 내용은 최대 40,000자 까지 입력 가능합니다.")
    private String content;
}