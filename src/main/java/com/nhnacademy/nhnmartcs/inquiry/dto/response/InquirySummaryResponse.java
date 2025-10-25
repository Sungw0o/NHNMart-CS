package com.nhnacademy.nhnmartcs.inquiry.dto.response;

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

    /**
     * Create an InquirySummaryResponse DTO from an Inquiry entity.
     *
     * Maps id, title, category description, createdAt formatted as ISO_LOCAL_DATE, and answered (true when the inquiry has an answer).
     *
     * @param inquiry the source Inquiry entity; may be null
     * @return the mapped InquirySummaryResponse, or null if {@code inquiry} is null
     */
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