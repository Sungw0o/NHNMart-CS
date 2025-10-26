package com.nhnacademy.nhnmartcs.inquiry.dto.response;

import com.nhnacademy.nhnmartcs.inquiry.domain.Answer;
import com.nhnacademy.nhnmartcs.inquiry.domain.Inquiry;
import lombok.Builder;
import lombok.Getter;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class InquiryDetailResponse {

    private Long inquiryId;
    private String title;
    private String category;
    private String content;
    private String createdAt;

    private boolean answered;
    private String answerContent;
    private String answerCreatedAt;
    private String answerAdminName;

    private List<AttachmentSummary> attachments;

    @Getter
    @Builder
    public static class AttachmentSummary {
        private String originalFilename;
        private String savedFilename;
    }

    public static InquiryDetailResponse fromEntity(Inquiry inquiry) {
        if (inquiry == null) {
            return null;
        }

        Answer answer = inquiry.getAnswer();

        List<Inquiry.FileInfo> fileInfoList = inquiry.getAttachedFiles() != null ? inquiry.getAttachedFiles() : Collections.emptyList();
        List<AttachmentSummary> attachmentSummaries = fileInfoList.stream()
                .map(fileInfo -> AttachmentSummary.builder()
                        .originalFilename(fileInfo.getOriginalFilename())
                        .savedFilename(fileInfo.getSavedFilename())
                        .build())
                .collect(Collectors.toList());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        return InquiryDetailResponse.builder()
                .inquiryId(inquiry.getInquiryId())
                .title(inquiry.getTitle())
                .category(inquiry.getCategory().getDescription())
                .content(inquiry.getContent())
                .createdAt(inquiry.getCreatedAt().format(formatter))
                .answered(answer != null)
                .answerContent(answer != null ? answer.getContent() : null)
                .answerCreatedAt(answer != null ? answer.getCreatedAt().format(formatter) : null)
                .answerAdminName(answer != null ? answer.getAdmin().getName() : null)
                .attachments(attachmentSummaries)
                .build();
    }
}