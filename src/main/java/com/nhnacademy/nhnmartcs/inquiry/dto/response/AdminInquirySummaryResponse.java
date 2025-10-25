package com.nhnacademy.nhnmartcs.inquiry.dto.response;

import com.nhnacademy.nhnmartcs.inquiry.domain.Inquiry;
import lombok.Builder;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
@Builder
public class AdminInquirySummaryResponse {
    private Long id;
    private String title;
    private String category;
    private String authorName;
    private String createdAt;

    public static AdminInquirySummaryResponse fromEntity(Inquiry inquiry) {
        if (inquiry == null) {
            return null;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        return AdminInquirySummaryResponse.builder()
                .id(inquiry.getInquiryId())
                .title(inquiry.getTitle())
                .category(inquiry.getCategory().getDescription())
                .authorName(inquiry.getCustomer().getName())
                .createdAt(inquiry.getCreatedAt().format(formatter))
                .build();
    }
}