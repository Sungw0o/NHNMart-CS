package com.nhnacademy.nhnmartcs.inquiry.domain;

import lombok.Getter;

@Getter
public enum InquiryCategory {

    COMPLAINT("불만 접수"),
    REFUND_EXCHANGE("환불/교환"),
    OTHER("기타 문의");

    private final String description;

    InquiryCategory(String description) {
        this.description = description;
    }


}
