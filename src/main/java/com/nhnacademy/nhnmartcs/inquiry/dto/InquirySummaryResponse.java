package com.nhnacademy.nhnmartcs.inquiry.dto;

import com.nhnacademy.nhnmartcs.inquiry.domain.Inquiry;
import lombok.Builder;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
@Builder
public class InquirySummaryResponse {

    private Long id;
    private String title;
    private String category;
    private String createdAt;
    private boolean answered;

    public static InquirySummaryResponse fromEntity(Inquiry inquiry) {
        if (inquiry == null) {
            return null;
        }
        return InquirySummaryResponse.builder()
                .id(inquiry.getInquiryId())
                .title(inquiry.getTitle())
                .category(inquiry.getCategory().getDescription())
                .createdAt(inquiry.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .answered(inquiry.getAnswer() != null)
                .build();
    }
}
