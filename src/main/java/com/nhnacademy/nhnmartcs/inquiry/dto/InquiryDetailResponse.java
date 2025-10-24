package com.nhnacademy.nhnmartcs.inquiry.dto;

import com.nhnacademy.nhnmartcs.attachment.domain.Attachment;
import com.nhnacademy.nhnmartcs.inquiry.domain.Answer;
import com.nhnacademy.nhnmartcs.inquiry.domain.Inquiry;
import lombok.Builder;
import lombok.Getter;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;


@Getter
@Builder
public class InquiryDetailResponse {


    private Long inquiryId;
    private String title;
    private String category;
    private String content;
    private String inquiryCreatedAt;


    private boolean answered;
    private String answerContent;
    private String answerCreatedAt;
    private String answerAdminName;

    private List<AttachmentSummary> attachments;

    @Getter
    @Builder
    public static class AttachmentSummary {
        private Long id;
        private String originalFilename;

    }


    public static InquiryDetailResponse fromEntity(Inquiry inquiry) {
        if (inquiry == null) {
            return null;
        }

        Answer answer = inquiry.getAnswer();
        List<Attachment> attachmentList = inquiry.getAttachments();

        List<AttachmentSummary> attachmentSummaries = attachmentList.stream()
                .map(att -> AttachmentSummary.builder()
                        .id(att.getId())
                        .originalFilename(att.getFilename())
                        .build())
                .collect(Collectors.toList());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        return InquiryDetailResponse.builder()
                .inquiryId(inquiry.getInquiryId())
                .title(inquiry.getTitle())
                .category(inquiry.getCategory().getDescription())
                .content(inquiry.getContent())
                .inquiryCreatedAt(inquiry.getCreatedAt().format(formatter))
                .answered(answer != null)
                .answerContent(answer != null ? answer.getContent() : null)
                .answerCreatedAt(answer != null ? answer.getCreatedAt().format(formatter) : null)
                .answerAdminName(answer != null ? answer.getAdmin().getName() : null)
                .attachments(attachmentSummaries)
                .build();
    }
}
