package com.nhnacademy.nhnmartcs.inquiry.service;

import com.nhnacademy.nhnmartcs.inquiry.dto.InquiryCreateRequest;
import com.nhnacademy.nhnmartcs.inquiry.dto.InquiryDetailResponse;
import com.nhnacademy.nhnmartcs.inquiry.dto.InquirySummaryResponse;
import com.nhnacademy.nhnmartcs.user.domain.Customer;

import java.util.List;

public interface InquiryService {

    Long createInquiry(Customer customer, InquiryCreateRequest requestDto);
    List<InquirySummaryResponse> getMyInquiries(Customer customer, String category);
    InquiryDetailResponse getInquiryDetail(Long inquiryId, Customer customer);

}
