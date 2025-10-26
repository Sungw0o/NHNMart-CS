package com.nhnacademy.nhnmartcs.inquiry.service.impl;

import com.nhnacademy.nhnmartcs.global.exception.InquiryAccessDeniedException;
import com.nhnacademy.nhnmartcs.global.exception.InquiryNotFoundException;
import com.nhnacademy.nhnmartcs.global.exception.InvalidFileTypeException;
import com.nhnacademy.nhnmartcs.inquiry.domain.Answer;
import com.nhnacademy.nhnmartcs.inquiry.domain.Inquiry;
import com.nhnacademy.nhnmartcs.inquiry.domain.InquiryCategory;
import com.nhnacademy.nhnmartcs.inquiry.dto.request.InquiryCreateRequest;
import com.nhnacademy.nhnmartcs.inquiry.dto.response.AdminInquirySummaryResponse;
import com.nhnacademy.nhnmartcs.inquiry.dto.response.InquiryDetailResponse;
import com.nhnacademy.nhnmartcs.inquiry.dto.response.InquirySummaryResponse;
import com.nhnacademy.nhnmartcs.inquiry.repository.InquiryRepository;
import com.nhnacademy.nhnmartcs.inquiry.service.InquiryService;
import com.nhnacademy.nhnmartcs.user.domain.CSAdmin;
import com.nhnacademy.nhnmartcs.user.domain.Customer;
import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j; // 제거
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InquiryServiceImpl implements InquiryService {

    private final InquiryRepository inquiryRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    private static final List<String> ALLOWED_IMAGE_TYPES = List.of("image/gif", "image/jpeg", "image/png");

    @Override
    public Long createInquiry(Customer customer, InquiryCreateRequest requestDto, List<MultipartFile> files) {

        List<Inquiry.FileInfo> savedFileInfos = new ArrayList<>();
        Path uploadPath = Paths.get(uploadDir);

        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            if (files != null) {
                for (MultipartFile file : files) {
                    if (file.isEmpty()) continue;

                    String contentType = file.getContentType();
                    if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
                        throw new InvalidFileTypeException("이미지 파일(GIF, JPG, PNG)만 업로드 가능합니다.");
                    }

                    String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
                    String savedFilename = UUID.randomUUID() + "_" + originalFilename;
                    Path targetLocation = uploadPath.resolve(savedFilename);

                    Files.copy(file.getInputStream(), targetLocation);

                    Inquiry.FileInfo fileInfo = new Inquiry.FileInfo(
                            originalFilename,
                            savedFilename,
                            targetLocation.toString()
                    );
                    savedFileInfos.add(fileInfo);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("파일 처리 중 오류가 발생했습니다.", e);
        }

        Inquiry inquiry = new Inquiry(
                null,
                requestDto.getTitle(),
                requestDto.getContent(),
                requestDto.getCategory(),
                LocalDateTime.now(),
                customer,
                null,
                savedFileInfos
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

    @Override
    public List<AdminInquirySummaryResponse> getUnansweredInquiries() {
        List<Inquiry> inquiries = inquiryRepository.findUnansweredInquiriesOrderByCreatedAtAsc();
        return inquiries.stream()
                .map(AdminInquirySummaryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public InquiryDetailResponse getInquiryDetailForAdmin(Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new InquiryNotFoundException("해당 문의를 찾을 수 없습니다. ID: " + inquiryId));
        return InquiryDetailResponse.fromEntity(inquiry);
    }

    @Override
    public void addAnswer(Long inquiryId, String answerContent, CSAdmin admin) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new InquiryNotFoundException("답변할 문의를 찾을 수 없습니다. ID: " + inquiryId));


        Answer newAnswer = new Answer(answerContent, admin);
        inquiry.addAnswer(newAnswer);
        inquiryRepository.save(inquiry);
    }
}