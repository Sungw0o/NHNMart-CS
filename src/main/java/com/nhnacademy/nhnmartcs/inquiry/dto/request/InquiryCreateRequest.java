package com.nhnacademy.nhnmartcs.inquiry.dto.request;

import com.nhnacademy.nhnmartcs.inquiry.domain.InquiryCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InquiryCreateRequest {

    @NotBlank(message="제목을 입력하세요")
    @Size(min=2, max=200 , message ="제목은 2자 이상 200자 이하로 해주세요.")
    private String title;

    @NotNull(message = "문의 분류를 선택해주세요.")
    private InquiryCategory category;

    @NotNull(message = "문의 내용을 입력해주세요.")
    @Size(max = 40000, message = "문의 내용은 최대 40,000자 까지 입력 가능합니다.")
    private String content;
}
