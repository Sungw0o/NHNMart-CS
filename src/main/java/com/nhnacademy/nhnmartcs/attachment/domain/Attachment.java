package com.nhnacademy.nhnmartcs.attachment.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Attachment {

    private Long id;
    private String filename;
    private String filePath;
    private String fileType;

}
