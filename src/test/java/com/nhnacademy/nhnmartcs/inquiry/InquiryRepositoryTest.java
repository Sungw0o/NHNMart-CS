package com.nhnacademy.nhnmartcs.inquiry;

import com.nhnacademy.nhnmartcs.inquiry.domain.Answer;
import com.nhnacademy.nhnmartcs.inquiry.domain.Inquiry;
import com.nhnacademy.nhnmartcs.inquiry.domain.InquiryCategory;
import com.nhnacademy.nhnmartcs.inquiry.repository.InquiryRepository;
import com.nhnacademy.nhnmartcs.inquiry.repository.impl.InquiryRepositoryImpl;
import com.nhnacademy.nhnmartcs.user.domain.CSAdmin;
import com.nhnacademy.nhnmartcs.user.domain.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

class InquiryRepositoryTest {

    private InquiryRepository inquiryRepository;

    private Customer customer1;
    private Customer customer2;
    private CSAdmin admin;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        inquiryRepository = new InquiryRepositoryImpl();

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

        Field storeField = InquiryRepositoryImpl.class.getDeclaredField("store");
        storeField.setAccessible(true);
        ((Map<Long, Inquiry>) storeField.get(null)).clear();

        Field sequenceField = InquiryRepositoryImpl.class.getDeclaredField("sequence");
        sequenceField.setAccessible(true);
        ((AtomicLong) sequenceField.get(null)).set(0L);
    }

    private Inquiry createTestInquiry(String title, Customer customer, InquiryCategory category, LocalDateTime createdAt) {
        return new Inquiry(null, title, "test content", category, createdAt, customer, null, Collections.emptyList());
    }

    @Test
    @DisplayName("새 문의 저장 시 ID 생성")
    void save_newInquiry() {
        Inquiry inquiry = createTestInquiry("첫 문의", customer1, InquiryCategory.COMPLAINT, LocalDateTime.now());
        Inquiry savedInquiry = inquiryRepository.save(inquiry);

        assertThat(savedInquiry.getInquiryId()).isEqualTo(1L);
        assertThat(savedInquiry.getTitle()).isEqualTo("첫 문의");
    }

    @Test
    @DisplayName("기존 문의 업데이트 (답변 추가 등)")
    void save_updateInquiry() {
        Inquiry inquiry = createTestInquiry("업데이트 전", customer1, InquiryCategory.COMPLAINT, LocalDateTime.now());
        Inquiry savedInquiry = inquiryRepository.save(inquiry);
        assertThat(savedInquiry.getInquiryId()).isEqualTo(1L);
        assertThat(savedInquiry.getAnswer()).isNull();

        Answer answer = new Answer("답변입니다.", admin);
        savedInquiry.addAnswer(answer);

        Inquiry updatedInquiry = inquiryRepository.save(savedInquiry);

        assertThat(updatedInquiry.getInquiryId()).isEqualTo(1L);
        assertThat(updatedInquiry.getAnswer()).isNotNull();
        assertThat(updatedInquiry.getAnswer().getContent()).isEqualTo("답변입니다.");
    }

    @Test
    @DisplayName("ID로 문의 찾기 - 성공")
    void findById_found() {
        Inquiry inquiry = createTestInquiry("문의 1", customer1, InquiryCategory.COMPLAINT, LocalDateTime.now());
        inquiryRepository.save(inquiry); // ID: 1L

        Optional<Inquiry> foundInquiry = inquiryRepository.findById(1L);

        assertThat(foundInquiry).isPresent();
        assertThat(foundInquiry.get().getInquiryId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("ID로 문의 찾기 - 실패")
    void findById_notFound() {
        Optional<Inquiry> foundInquiry = inquiryRepository.findById(999L);
        assertThat(foundInquiry).isNotPresent();
    }

    @Test
    @DisplayName("고객 ID로 문의 목록 조회 (최신순 정렬)")
    void findByCustomerOrderByCreatedAtDesc() {
        LocalDateTime now = LocalDateTime.now();
        Inquiry inquiry1 = createTestInquiry("문의 1", customer1, InquiryCategory.COMPLAINT, now.minusDays(2));
        Inquiry inquiry2 = createTestInquiry("문의 2", customer2, InquiryCategory.PROPOSAL, now.minusDays(1)); // 다른 고객
        Inquiry inquiry3 = createTestInquiry("문의 3", customer1, InquiryCategory.REFUND_EXCHANGE, now); // 최신

        inquiryRepository.save(inquiry1);
        inquiryRepository.save(inquiry2);
        inquiryRepository.save(inquiry3);

        List<Inquiry> inquiries = inquiryRepository.findByCustomerOrderByCreatedAtDesc(customer1);

        assertThat(inquiries).hasSize(2);
        assertThat(inquiries.get(0).getTitle()).isEqualTo("문의 3"); // 최신순
        assertThat(inquiries.get(1).getTitle()).isEqualTo("문의 1");
    }

    @Test
    @DisplayName("고객 ID와 카테고리로 문의 목록 조회 (최신순 정렬)")
    void findByCustomerAndCategoryOrderByCreatedAtDesc() {
        LocalDateTime now = LocalDateTime.now();
        Inquiry inquiry1 = createTestInquiry("불만 1", customer1, InquiryCategory.COMPLAINT, now.minusDays(2));
        Inquiry inquiry2 = createTestInquiry("제안 1", customer1, InquiryCategory.PROPOSAL, now.minusDays(1));
        Inquiry inquiry3 = createTestInquiry("불만 2", customer1, InquiryCategory.COMPLAINT, now); // 최신 불만

        inquiryRepository.save(inquiry1);
        inquiryRepository.save(inquiry2);
        inquiryRepository.save(inquiry3);

        List<Inquiry> inquiries = inquiryRepository.findByCustomerAndCategoryOrderByCreatedAtDesc(customer1, InquiryCategory.COMPLAINT);

        assertThat(inquiries).hasSize(2);
        assertThat(inquiries.get(0).getTitle()).isEqualTo("불만 2"); // 최신순
        assertThat(inquiries.get(1).getTitle()).isEqualTo("불만 1");
    }

    @Test
    @DisplayName("미답변 문의 목록 조회 (오래된순 정렬)")
    void findUnansweredInquiriesOrderByCreatedAtAsc() {
        LocalDateTime now = LocalDateTime.now();
        Inquiry inquiry1 = createTestInquiry("미답변 1 (오래됨)", customer1, InquiryCategory.COMPLAINT, now.minusDays(2));
        Inquiry inquiry2 = createTestInquiry("답변 1", customer1, InquiryCategory.PROPOSAL, now.minusDays(1));
        inquiry2.addAnswer(new Answer("답변", admin));
        Inquiry inquiry3 = createTestInquiry("미답변 2 (최신)", customer2, InquiryCategory.OTHER, now);

        inquiryRepository.save(inquiry1);
        inquiryRepository.save(inquiry2);
        inquiryRepository.save(inquiry3);

        List<Inquiry> inquiries = inquiryRepository.findUnansweredInquiriesOrderByCreatedAtAsc();

        assertThat(inquiries).hasSize(2);
        assertThat(inquiries.get(0).getTitle()).isEqualTo("미답변 1 (오래됨)"); // 오래된순
        assertThat(inquiries.get(1).getTitle()).isEqualTo("미답변 2 (최신)");
    }
}