package com.nhnacademy.nhnmartcs.inquiry.domain;

import com.nhnacademy.nhnmartcs.user.domain.Customer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Inquiry {

    private Long inquiryId;
    private String title;
    private String content;
    private InquiryCategory category;
    private LocalDateTime createdAt;

    private Customer customer;
    private Answer answer;

    private List<FileInfo> attachedFiles = new ArrayList<>();

    public void addAnswer(Answer answer){
        this.answer = answer;
    }

    public void addAttachedFiles(List<FileInfo> files) {
        if (this.attachedFiles == null) {
            this.attachedFiles = new ArrayList<>();
        }
        this.attachedFiles.addAll(files);
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class FileInfo {
        private String originalFilename;
        private String savedFilename;
        private String filePath;
    }
}