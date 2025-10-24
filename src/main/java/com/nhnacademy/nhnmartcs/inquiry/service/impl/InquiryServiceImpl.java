package com.nhnacademy.nhnmartcs.inquiry.service.impl;

import com.nhnacademy.nhnmartcs.global.exception.InquiryAccessDeniedException;
import com.nhnacademy.nhnmartcs.global.exception.InquiryNotFoundException;
import com.nhnacademy.nhnmartcs.inquiry.domain.Inquiry;
import com.nhnacademy.nhnmartcs.inquiry.domain.InquiryCategory;
import com.nhnacademy.nhnmartcs.inquiry.dto.InquiryCreateRequest;
import com.nhnacademy.nhnmartcs.inquiry.dto.InquiryDetailResponse;
import com.nhnacademy.nhnmartcs.inquiry.dto.InquirySummaryResponse;
import com.nhnacademy.nhnmartcs.inquiry.repository.InquiryRepository;
import com.nhnacademy.nhnmartcs.inquiry.service.InquiryService;
import com.nhnacademy.nhnmartcs.user.domain.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InquiryServiceImpl implements InquiryService {

    private final InquiryRepository inquiryRepository;
    // private final AttachmentService attachmentService; // 파일 첨부 시 주입

    @Override
    public Long createInquiry(Customer customer, InquiryCreateRequest requestDto) {
        // List<Attachment> savedAttachments = attachmentService.saveFiles(files);

        Inquiry inquiry = new Inquiry(
                null,
                requestDto.getTitle(),
                requestDto.getContent(),
                requestDto.getCategory(),
                LocalDateTime.now(),
                customer,
                null,
                Collections.emptyList()
        );

        Inquiry savedInquiry = inquiryRepository.save(inquiry);
        return savedInquiry.getInquiryId();
    }

    @Override
    public List<InquirySummaryResponse> getMyInquiries(Customer customer, String category) {
        List<Inquiry> inquiries;
        if (StringUtils.hasText(category)) {
            try {
                InquiryCategory categoryEnum = InquiryCategory.valueOf(category.toUpperCase());
                inquiries = inquiryRepository.findByCustomerAndCategoryOrderByCreatedAtDesc(customer, categoryEnum);
            } catch (IllegalArgumentException e) {
                return Collections.emptyList();
            }
        } else {

            inquiries = inquiryRepository.findByCustomerOrderByCreatedAtDesc(customer);
        }
        return inquiries.stream()
                .map(InquirySummaryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public InquiryDetailResponse getInquiryDetail(Long inquiryId, Customer customer) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new InquiryNotFoundException("해당 문의를 찾을 수 없습니다. ID: " + inquiryId));
        if (!inquiry.getCustomer().getUserId().equals(customer.getUserId())) {
            throw new InquiryAccessDeniedException("본인의 문의만 조회할 수 있습니다.");
        }

        return InquiryDetailResponse.fromEntity(inquiry);
    }
}
