package com.nhnacademy.nhnmartcs.inquiry;

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
import com.nhnacademy.nhnmartcs.inquiry.service.impl.InquiryServiceImpl;
import com.nhnacademy.nhnmartcs.user.domain.CSAdmin;
import com.nhnacademy.nhnmartcs.user.domain.Customer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

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
    private AutoCloseable openMocks;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        openMocks = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(inquiryService, "uploadDir", tempDir.toString());

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
                LocalDateTime.now().minusDays(1), customer1, null, new ArrayList<>()
        );

        Answer answer = new Answer("답변 완료", admin);
        List<Inquiry.FileInfo> sampleFiles = List.of(
                new Inquiry.FileInfo("sample.jpg", "uuid_sample.jpg", tempDir.resolve("uuid_sample.jpg").toString())
        );
        testInquiryAnswered = new Inquiry(
                2L, "답변된 문의", "내용", InquiryCategory.PROPOSAL,
                LocalDateTime.now().minusDays(2), customer2, answer, sampleFiles
        );

        when(inquiryRepository.save(any(Inquiry.class))).thenAnswer(invocation -> {
            Inquiry inquiry = invocation.getArgument(0);
            if (inquiry.getInquiryId() == null) {
                inquiry.setInquiryId(System.currentTimeMillis());
            }
            return inquiry;
        });
        when(inquiryRepository.findById(anyLong())).thenReturn(Optional.empty());
        when(inquiryRepository.findById(1L)).thenReturn(Optional.of(testInquiry));
        when(inquiryRepository.findById(2L)).thenReturn(Optional.of(testInquiryAnswered));
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
    }

    @Test
    @DisplayName("문의 생성 성공 (첨부파일 없음)")
    void createInquiry_success_noFiles() {
        InquiryCreateRequest request = new InquiryCreateRequest();
        request.setTitle("새 문의");
        request.setContent("내용입니다");
        request.setCategory(InquiryCategory.COMPLAINT);

        Long inquiryId = inquiryService.createInquiry(customer1, request, Collections.emptyList());

        assertThat(inquiryId).isNotNull();
        ArgumentCaptor<Inquiry> captor = ArgumentCaptor.forClass(Inquiry.class);
        verify(inquiryRepository).save(captor.capture());
        assertThat(captor.getValue().getTitle()).isEqualTo("새 문의");
        assertThat(captor.getValue().getCustomer()).isEqualTo(customer1);
        assertThat(captor.getValue().getAttachedFiles()).isEmpty();
    }

    @Test
    @DisplayName("문의 생성 성공 (첨부파일 포함)")
    void createInquiry_success_withFiles() throws IOException {
        InquiryCreateRequest request = new InquiryCreateRequest();
        request.setTitle("첨부파일 있는 문의");
        request.setContent("이미지 첨부");
        request.setCategory(InquiryCategory.PROPOSAL);

        MockMultipartFile file1 = new MockMultipartFile(
                "files", "test1.png", MediaType.IMAGE_PNG_VALUE, "content1".getBytes()
        );
        MockMultipartFile file2 = new MockMultipartFile(
                "files", "test2.jpg", MediaType.IMAGE_JPEG_VALUE, "content2".getBytes()
        );
        List<MultipartFile> files = List.of(file1, file2);

        Long inquiryId = inquiryService.createInquiry(customer1, request, files);

        assertThat(inquiryId).isNotNull();
        ArgumentCaptor<Inquiry> captor = ArgumentCaptor.forClass(Inquiry.class);
        verify(inquiryRepository).save(captor.capture());
        Inquiry savedInquiry = captor.getValue();
        assertThat(savedInquiry.getAttachedFiles()).hasSize(2);
        assertThat(Files.exists(tempDir.resolve(savedInquiry.getAttachedFiles().get(0).getSavedFilename()))).isTrue();
        assertThat(Files.exists(tempDir.resolve(savedInquiry.getAttachedFiles().get(1).getSavedFilename()))).isTrue();
    }

    @Test
    @DisplayName("문의 생성 실패 (잘못된 파일 타입)")
    void createInquiry_fail_invalidFileType() {
        InquiryCreateRequest request = new InquiryCreateRequest();
        request.setTitle("잘못된 파일");
        request.setContent("텍스트 파일 첨부 시도");
        request.setCategory(InquiryCategory.OTHER);

        MockMultipartFile invalidFile = new MockMultipartFile(
                "files", "test.txt", MediaType.TEXT_PLAIN_VALUE, "content".getBytes()
        );
        List<MultipartFile> files = List.of(invalidFile);

        assertThatThrownBy(() -> inquiryService.createInquiry(customer1, request, files))
                .isInstanceOf(InvalidFileTypeException.class)
                .hasMessage("이미지 파일(GIF, JPG, PNG)만 업로드 가능합니다.");

        verify(inquiryRepository, never()).save(any(Inquiry.class));
    }

//    @Test
//    @DisplayName("문의 생성 실패 (파일 저장 중 IOException)")
//    void createInquiry_fail_ioException() throws IOException {
//        InquiryCreateRequest request = new InquiryCreateRequest();
//        request.setTitle("IO 에러");
//        request.setContent("파일 저장 실패 테스트");
//        request.setCategory(InquiryCategory.COMPLAINT);
//
//        MockMultipartFile file = new MockMultipartFile(
//                "files", "test.png", MediaType.IMAGE_PNG_VALUE, "content".getBytes()
//        );
//        List<MultipartFile> files = List.of(file);
//
//        Path invalidDir = tempDir.resolve("invalid_subdir");
//        ReflectionTestUtils.setField(inquiryService, "uploadDir", invalidDir.toString());
//
//        assertThatThrownBy(() -> inquiryService.createInquiry(customer1, request, files))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessageContaining("파일 처리 중 오류가 발생했습니다.");
//
//        verify(inquiryRepository, never()).save(any(Inquiry.class));
//    }

    @Test
    @DisplayName("내 문의 목록 조회 - 카테고리 없음")
    void getMyInquiries_withoutCategory() {
        when(inquiryRepository.findByCustomerOrderByCreatedAtDesc(customer1))
                .thenReturn(List.of(testInquiry, testInquiryAnswered));

        List<InquirySummaryResponse> responses = inquiryService.getMyInquiries(customer1, null);

        assertThat(responses).hasSize(2);
        verify(inquiryRepository).findByCustomerOrderByCreatedAtDesc(customer1);
    }

    @Test
    @DisplayName("내 문의 목록 조회 - 카테고리 지정")
    void getMyInquiries_withCategory() {
        when(inquiryRepository.findByCustomerAndCategoryOrderByCreatedAtDesc(customer1, InquiryCategory.COMPLAINT))
                .thenReturn(List.of(testInquiry));

        List<InquirySummaryResponse> responses = inquiryService.getMyInquiries(customer1, "COMPLAINT");

        assertThat(responses).hasSize(1);
        verify(inquiryRepository).findByCustomerAndCategoryOrderByCreatedAtDesc(customer1, InquiryCategory.COMPLAINT);
    }

    @Test
    @DisplayName("내 문의 목록 조회 - 잘못된 카테고리")
    void getMyInquiries_invalidCategory() {
        List<InquirySummaryResponse> responses = inquiryService.getMyInquiries(customer1, "INVALID_CATEGORY");
        assertThat(responses).isEmpty();
        verify(inquiryRepository, never()).findByCustomerAndCategoryOrderByCreatedAtDesc(any(), any());
    }

    @Test
    @DisplayName("문의 상세 조회 - 성공 (본인, 첨부파일 없음)")
    void getInquiryDetail_success_noFiles() {
        when(inquiryRepository.findById(1L)).thenReturn(Optional.of(testInquiry));
        InquiryDetailResponse response = inquiryService.getInquiryDetail(1L, customer1);
        assertThat(response.getTitle()).isEqualTo("테스트 문의");
        verify(inquiryRepository).findById(1L);
    }

    @Test
    @DisplayName("문의 상세 조회 - 성공 (본인, 첨부파일 있음)")
    void getInquiryDetail_success_withFiles() {
        when(inquiryRepository.findById(2L)).thenReturn(Optional.of(testInquiryAnswered));
        InquiryDetailResponse response = inquiryService.getInquiryDetail(2L, customer2);
        assertThat(response.getAttachments()).hasSize(1);
        verify(inquiryRepository).findById(2L);
    }

    @Test
    @DisplayName("문의 상세 조회 - 실패 (문의 없음)")
    void getInquiryDetail_notFound() {
        when(inquiryRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> inquiryService.getInquiryDetail(99L, customer1))
                .isInstanceOf(InquiryNotFoundException.class);
        verify(inquiryRepository).findById(99L);
    }

    @Test
    @DisplayName("문의 상세 조회 - 실패 (타인 문의)")
    void getInquiryDetail_accessDenied() {
        when(inquiryRepository.findById(1L)).thenReturn(Optional.of(testInquiry));
        assertThatThrownBy(() -> inquiryService.getInquiryDetail(1L, customer2))
                .isInstanceOf(InquiryAccessDeniedException.class);
        verify(inquiryRepository).findById(1L);
    }

    @Test
    @DisplayName("미답변 문의 목록 조회 (관리자)")
    void getUnansweredInquiries() {
        when(inquiryRepository.findUnansweredInquiriesOrderByCreatedAtAsc()).thenReturn(List.of(testInquiry));
        List<AdminInquirySummaryResponse> responses = inquiryService.getUnansweredInquiries();
        assertThat(responses).hasSize(1);
        verify(inquiryRepository).findUnansweredInquiriesOrderByCreatedAtAsc();
    }

    @Test
    @DisplayName("문의 상세 조회 (관리자) - 성공")
    void getInquiryDetailForAdmin_success() {
        when(inquiryRepository.findById(2L)).thenReturn(Optional.of(testInquiryAnswered));
        InquiryDetailResponse response = inquiryService.getInquiryDetailForAdmin(2L);
        assertThat(response.isAnswered()).isTrue();
        verify(inquiryRepository).findById(2L);
    }

    @Test
    @DisplayName("문의 상세 조회 (관리자) - 실패 (문의 없음)")
    void getInquiryDetailForAdmin_notFound() {
        when(inquiryRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> inquiryService.getInquiryDetailForAdmin(99L))
                .isInstanceOf(InquiryNotFoundException.class);
        verify(inquiryRepository).findById(99L);
    }

    @Test
    @DisplayName("답변 등록 - 성공")
    void addAnswer_success() {
        when(inquiryRepository.findById(1L)).thenReturn(Optional.of(testInquiry));
        inquiryService.addAnswer(1L, "새로운 답변", admin);
        ArgumentCaptor<Inquiry> captor = ArgumentCaptor.forClass(Inquiry.class);
        verify(inquiryRepository).save(captor.capture());
        assertThat(captor.getValue().getAnswer().getContent()).isEqualTo("새로운 답변");
    }

    @Test
    @DisplayName("답변 등록 - 실패 (문의 없음)")
    void addAnswer_notFound() {
        when(inquiryRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> inquiryService.addAnswer(99L, "답변", admin))
                .isInstanceOf(InquiryNotFoundException.class);
        verify(inquiryRepository).findById(99L);
    }

    @Test
    @DisplayName("답변 등록 - 이미 답변된 문의 (덮어쓰기)")
    void addAnswer_alreadyAnswered_overwrites() {
        when(inquiryRepository.findById(2L)).thenReturn(Optional.of(testInquiryAnswered));
        inquiryService.addAnswer(2L, "수정된 답변", admin);
        ArgumentCaptor<Inquiry> captor = ArgumentCaptor.forClass(Inquiry.class);
        verify(inquiryRepository).save(captor.capture());
        assertThat(captor.getValue().getAnswer().getContent()).isEqualTo("수정된 답변");
    }
}
