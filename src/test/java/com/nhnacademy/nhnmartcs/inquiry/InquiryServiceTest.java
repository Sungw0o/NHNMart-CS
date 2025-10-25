package com.nhnacademy.nhnmartcs.inquiry;

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
import com.nhnacademy.nhnmartcs.inquiry.service.impl.InquiryServiceImpl;
import com.nhnacademy.nhnmartcs.user.domain.CSAdmin;
import com.nhnacademy.nhnmartcs.user.domain.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InquiryServiceTest {

    @Mock
    private InquiryRepository inquiryRepository;

    @InjectMocks
    private InquiryServiceImpl inquiryService;

    private Customer customer1;
    private Customer customer2;
    private CSAdmin admin;
    private Inquiry testInquiry;
    private Inquiry testInquiryAnswered;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        customer1 = new Customer();
        customer1.setUserId(1L);
        customer1.setLoginId("customer1");
        customer1.setName("고객1");

        customer2 = new Customer();
        customer2.setUserId(2L);
        customer2.setLoginId("customer2");
        customer2.setName("고객2");

        admin = new CSAdmin();
        admin.setUserId(99L);
        admin.setLoginId("admin");
        admin.setName("관리자");

        testInquiry = new Inquiry(
                1L, "테스트 문의", "내용", InquiryCategory.COMPLAINT,
                LocalDateTime.now().minusDays(1), customer1, null, Collections.emptyList()
        );

        Answer answer = new Answer("답변 완료", admin);
        testInquiryAnswered = new Inquiry(
                2L, "답변된 문의", "내용", InquiryCategory.PROPOSAL,
                LocalDateTime.now().minusDays(2), customer2, answer, Collections.emptyList()
        );
    }

    @Test
    @DisplayName("문의 생성 성공")
    void createInquiry_success() {
        InquiryCreateRequest request = new InquiryCreateRequest();
        request.setTitle("새 문의");
        request.setContent("내용입니다");
        request.setCategory(InquiryCategory.COMPLAINT);

        when(inquiryRepository.save(any(Inquiry.class))).thenAnswer(invocation -> {
            Inquiry inquiry = invocation.getArgument(0);
            inquiry.setInquiryId(1L);
            return inquiry;
        });

        Long inquiryId = inquiryService.createInquiry(customer1, request);

        assertThat(inquiryId).isEqualTo(1L);
        ArgumentCaptor<Inquiry> captor = ArgumentCaptor.forClass(Inquiry.class);
        verify(inquiryRepository).save(captor.capture());
        assertThat(captor.getValue().getTitle()).isEqualTo("새 문의");
        assertThat(captor.getValue().getCustomer()).isEqualTo(customer1);
    }

    @Test
    @DisplayName("내 문의 목록 조회 - 카테고리 없음")
    void getMyInquiries_withoutCategory() {
        when(inquiryRepository.findByCustomerOrderByCreatedAtDesc(customer1))
                .thenReturn(List.of(testInquiry, testInquiryAnswered));

        List<InquirySummaryResponse> responses = inquiryService.getMyInquiries(customer1, null);

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getTitle()).isEqualTo(testInquiry.getTitle());
    }

    @Test
    @DisplayName("내 문의 목록 조회 - 카테고리 지정")
    void getMyInquiries_withCategory() {
        when(inquiryRepository.findByCustomerAndCategoryOrderByCreatedAtDesc(customer1, InquiryCategory.COMPLAINT))
                .thenReturn(List.of(testInquiry));

        List<InquirySummaryResponse> responses = inquiryService.getMyInquiries(customer1, "COMPLAINT");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getTitle()).isEqualTo("테스트 문의");
    }

    @Test
    @DisplayName("내 문의 목록 조회 - 잘못된 카테고리")
    void getMyInquiries_invalidCategory() {
        List<InquirySummaryResponse> responses = inquiryService.getMyInquiries(customer1, "INVALID_CATEGORY");
        assertThat(responses).isEmpty();
    }

    @Test
    @DisplayName("문의 상세 조회 - 성공 (본인)")
    void getInquiryDetail_success() {
        when(inquiryRepository.findById(1L)).thenReturn(Optional.of(testInquiry));

        InquiryDetailResponse response = inquiryService.getInquiryDetail(1L, customer1);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("테스트 문의");
        assertThat(response.isAnswered()).isFalse();
    }

    @Test
    @DisplayName("문의 상세 조회 - 실패 (문의 없음)")
    void getInquiryDetail_notFound() {
        when(inquiryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inquiryService.getInquiryDetail(99L, customer1))
                .isInstanceOf(InquiryNotFoundException.class)
                .hasMessageContaining("ID: 99");
    }

    @Test
    @DisplayName("문의 상세 조회 - 실패 (타인 문의)")
    void getInquiryDetail_accessDenied() {
        when(inquiryRepository.findById(1L)).thenReturn(Optional.of(testInquiry));

        assertThatThrownBy(() -> inquiryService.getInquiryDetail(1L, customer2))
                .isInstanceOf(InquiryAccessDeniedException.class)
                .hasMessage("본인의 문의만 조회할 수 있습니다.");
    }

    @Test
    @DisplayName("미답변 문의 목록 조회 (관리자)")
    void getUnansweredInquiries() {
        when(inquiryRepository.findUnansweredInquiriesOrderByCreatedAtAsc()).thenReturn(List.of(testInquiry));

        List<AdminInquirySummaryResponse> responses = inquiryService.getUnansweredInquiries();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getTitle()).isEqualTo("테스트 문의");
        assertThat(responses.get(0).getAuthorName()).isEqualTo(customer1.getName());
    }

    @Test
    @DisplayName("문의 상세 조회 (관리자) - 성공")
    void getInquiryDetailForAdmin_success() {
        when(inquiryRepository.findById(2L)).thenReturn(Optional.of(testInquiryAnswered));

        InquiryDetailResponse response = inquiryService.getInquiryDetailForAdmin(2L);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("답변된 문의");
        assertThat(response.isAnswered()).isTrue();
        assertThat(response.getAnswerAdminName()).isEqualTo(admin.getName());
    }

    @Test
    @DisplayName("문의 상세 조회 (관리자) - 실패 (문의 없음)")
    void getInquiryDetailForAdmin_notFound() {
        when(inquiryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inquiryService.getInquiryDetailForAdmin(99L))
                .isInstanceOf(InquiryNotFoundException.class);
    }

    @Test
    @DisplayName("답변 등록 - 성공")
    void addAnswer_success() {
        when(inquiryRepository.findById(1L)).thenReturn(Optional.of(testInquiry));

        inquiryService.addAnswer(1L, "새로운 답변", admin);

        ArgumentCaptor<Inquiry> captor = ArgumentCaptor.forClass(Inquiry.class);
        verify(inquiryRepository).save(captor.capture());

        Inquiry savedInquiry = captor.getValue();
        assertThat(savedInquiry.getAnswer()).isNotNull();
        assertThat(savedInquiry.getAnswer().getContent()).isEqualTo("새로운 답변");
        assertThat(savedInquiry.getAnswer().getAdmin()).isEqualTo(admin);
    }

    @Test
    @DisplayName("답변 등록 - 실패 (문의 없음)")
    void addAnswer_notFound() {
        when(inquiryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inquiryService.addAnswer(99L, "답변", admin))
                .isInstanceOf(InquiryNotFoundException.class)
                .hasMessageContaining("ID: 99");
    }

}