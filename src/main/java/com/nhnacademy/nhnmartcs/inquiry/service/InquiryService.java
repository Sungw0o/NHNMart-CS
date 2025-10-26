package com.nhnacademy.nhnmartcs.inquiry.service;

import com.nhnacademy.nhnmartcs.inquiry.dto.request.InquiryCreateRequest;
import com.nhnacademy.nhnmartcs.inquiry.dto.response.AdminInquirySummaryResponse;
import com.nhnacademy.nhnmartcs.inquiry.dto.response.InquiryDetailResponse;
import com.nhnacademy.nhnmartcs.inquiry.dto.response.InquirySummaryResponse;
import com.nhnacademy.nhnmartcs.user.domain.CSAdmin;
import com.nhnacademy.nhnmartcs.user.domain.Customer;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface InquiryService {

    Long createInquiry(Customer customer, InquiryCreateRequest requestDto, List<MultipartFile> files);    List<InquirySummaryResponse> getMyInquiries(Customer customer, String category);
    InquiryDetailResponse getInquiryDetail(Long inquiryId, Customer customer);
    List<AdminInquirySummaryResponse> getUnansweredInquiries();
    InquiryDetailResponse getInquiryDetailForAdmin(Long inquiryId);
    void addAnswer(Long inquiryId, String answerContent, CSAdmin admin);
}
