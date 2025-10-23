package com.nhnacademy.nhnmartcs.inquiry.domain;

import com.nhnacademy.nhnmartcs.attachment.domain.Attachment;
import com.nhnacademy.nhnmartcs.user.domain.Customer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class Inquiry {

    private Long inquiryId;
    private String title;
    private String content;
    private InquiryCategory category;
    private LocalDateTime createdAt;

    private Customer customer;
    private Answer answer;
    private List<Attachment> attachments = new ArrayList<>();

    public void addAnswer(Answer answer){
        this.answer = answer;
    }

    public void addAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }
}
