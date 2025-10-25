package com.nhnacademy.nhnmartcs.inquiry.service.impl;

import com.nhnacademy.nhnmartcs.global.exception.InquiryAccessDeniedException;
import com.nhnacademy.nhnmartcs.global.exception.InquiryNotFoundException;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InquiryServiceImpl implements InquiryService {

    private final InquiryRepository inquiryRepository;
    /**
     * Create and persist a new inquiry for the given customer.
     *
     * Constructs an Inquiry from the provided request data, saves it to the repository,
     * and returns the saved inquiry's identifier.
     *
     * @param customer   the customer who created the inquiry
     * @param requestDto request data containing the inquiry title, content, and category
     * @return the generated inquiry ID
     */

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

    /**
     * Retrieve the given customer's inquiries, optionally filtered by category.
     *
     * <p>If a category name is provided, it is matched case-insensitively to an InquiryCategory.
     * If the provided category name does not correspond to any enum value, an empty list is returned.</p>
     *
     * @param customer the customer whose inquiries to fetch
     * @param category optional category name (case-insensitive); may be null or empty to fetch all categories
     * @return a list of InquirySummaryResponse objects for the customer's inquiries, ordered by creation time descending; empty if no matching inquiries or if an invalid category name was provided
     */
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

    /**
     * Retrieve detailed information for an inquiry if the given customer owns it.
     *
     * @param inquiryId the identifier of the inquiry to retrieve
     * @param customer  the customer requesting the inquiry; used to verify ownership
     * @return the inquiry details as an {@code InquiryDetailResponse}
     * @throws InquiryNotFoundException    if no inquiry exists with the given id
     * @throws InquiryAccessDeniedException if the requesting customer does not own the inquiry
     */
    @Override
    public InquiryDetailResponse getInquiryDetail(Long inquiryId, Customer customer) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new InquiryNotFoundException("해당 문의를 찾을 수 없습니다. ID: " + inquiryId));
        if (!inquiry.getCustomer().getUserId().equals(customer.getUserId())) {
            throw new InquiryAccessDeniedException("본인의 문의만 조회할 수 있습니다.");
        }

        return InquiryDetailResponse.fromEntity(inquiry);
    }

    /**
     * Retrieves all inquiries that have not been answered, ordered by creation time ascending, for administrative overview.
     *
     * @return a list of AdminInquirySummaryResponse representing unanswered inquiries ordered by creation time (oldest first)
     */
    @Override
    public List<AdminInquirySummaryResponse> getUnansweredInquiries() {
        List<Inquiry> inquiries = inquiryRepository.findUnansweredInquiriesOrderByCreatedAtAsc();

        return inquiries.stream()
                .map(AdminInquirySummaryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve detailed information for a specific inquiry for administrative viewing.
     *
     * @param inquiryId the ID of the inquiry to retrieve
     * @return an InquiryDetailResponse representing the inquiry
     * @throws InquiryNotFoundException if no inquiry exists with the given ID
     */
    @Override
    public InquiryDetailResponse getInquiryDetailForAdmin(Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new InquiryNotFoundException("해당 문의를 찾을 수 없습니다. ID: " + inquiryId));

        return InquiryDetailResponse.fromEntity(inquiry);
    }

    /**
     * Add an answer to an existing inquiry on behalf of a customer support admin.
     *
     * @param inquiryId     the ID of the inquiry to which the answer will be added
     * @param answerContent the text content of the answer
     * @param admin         the admin creating the answer
     * @throws InquiryNotFoundException if no inquiry exists with the given ID
     */
    @Override
    public void addAnswer(Long inquiryId, String answerContent, CSAdmin admin) {
        // 1. 답변할 원본 문의 조회
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new InquiryNotFoundException("답변할 문의를 찾을 수 없습니다. ID: " + inquiryId));

        // 2. Answer 객체 생성 (Answer.java 생성자 활용)
        Answer newAnswer = new Answer(answerContent, admin);

        // 3. Inquiry 객체에 답변 추가 (Inquiry.java 메서드 활용)
        inquiry.addAnswer(newAnswer);

        // 4. Map 저장소는 덮어쓰기(update)를 지원하므로 save 호출
        inquiryRepository.save(inquiry);

        log.info("Answer added successfully by admin: {} to inquiry ID: {}", admin.getLoginId(), inquiryId);
    }
}