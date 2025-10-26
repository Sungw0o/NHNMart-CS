package com.nhnacademy.nhnmartcs.inquiry;

import com.nhnacademy.nhnmartcs.global.exception.InquiryAccessDeniedException;
import com.nhnacademy.nhnmartcs.global.exception.InquiryNotFoundException;
import com.nhnacademy.nhnmartcs.global.exception.InvalidFileTypeException;
import com.nhnacademy.nhnmartcs.inquiry.controller.CustomerController;
import com.nhnacademy.nhnmartcs.inquiry.dto.request.InquiryCreateRequest;
import com.nhnacademy.nhnmartcs.inquiry.dto.response.InquiryDetailResponse;
import com.nhnacademy.nhnmartcs.inquiry.dto.response.InquirySummaryResponse;
import com.nhnacademy.nhnmartcs.inquiry.service.InquiryService;
import com.nhnacademy.nhnmartcs.user.domain.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InquiryService inquiryService;

    private Customer testCustomer;
    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setUserId(1L);
        testCustomer.setLoginId("customer1");
        testCustomer.setName("고객1");

        session = new MockHttpSession();
        session.setAttribute("loginUser", testCustomer);
    }

    @Test
    @DisplayName("GET /cs - 내 문의 목록 (로그인 상태)")
    void viewMyInquiries_loggedIn() throws Exception {
        InquirySummaryResponse summary = InquirySummaryResponse.builder()
                .id(1L)
                .title("테스트")
                .category("불만")
                .answered(false)
                .createdAt("2025-10-25")
                .build();
        when(inquiryService.getMyInquiries(testCustomer, null)).thenReturn(List.of(summary));

        mockMvc.perform(MockMvcRequestBuilders.get("/cs").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("inquiry-list"))
                .andExpect(model().attributeExists("inquiries"))
                .andExpect(model().attribute("inquiries", List.of(summary)));
    }

    @Test
    @DisplayName("GET /cs - 카테고리 필터링")
    void viewMyInquiries_withCategory() throws Exception {
        when(inquiryService.getMyInquiries(testCustomer, "COMPLAINT")).thenReturn(Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders.get("/cs").session(session).param("category", "COMPLAINT"))
                .andExpect(status().isOk())
                .andExpect(view().name("inquiry-list"))
                .andExpect(model().attribute("selectedCategory", "COMPLAINT"));
    }

    @Test
    @DisplayName("GET /cs - (로그아웃 상태, 인터셉터)")
    void viewMyInquiries_loggedOut() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/cs"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/cs/login"));
    }

    @Test
    @DisplayName("GET /cs/inquiry - 문의 작성 폼")
    void inquiryForm() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/cs/inquiry").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("inquiry-form"))
                .andExpect(model().attributeExists("inquiryCreateRequest"))
                .andExpect(model().attributeExists("categories"));
    }

    @Test
    @DisplayName("POST /cs/inquiry - 문의 생성 성공 (첨부파일 없음)")
    void createInquiry_success_noFiles() throws Exception {
        when(inquiryService.createInquiry(eq(testCustomer), any(InquiryCreateRequest.class), anyList())).thenReturn(1L);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/cs/inquiry")
                        .session(session)
                        .param("title", "테스트 제목")
                        .param("category", "COMPLAINT")
                        .param("content", "테스트 내용입니다."))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/cs"));
    }

    @Test
    @DisplayName("POST /cs/inquiry - 문의 생성 성공 (첨부파일 포함)")
    void createInquiry_success_withFiles() throws Exception {
        when(inquiryService.createInquiry(eq(testCustomer), any(InquiryCreateRequest.class), anyList())).thenReturn(1L);

        MockMultipartFile file1 = new MockMultipartFile(
                "files",
                "hello.png",
                MediaType.IMAGE_PNG_VALUE,
                "Hello, World!".getBytes()
        );
        MockMultipartFile file2 = new MockMultipartFile(
                "files",
                "world.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "World, Hello!".getBytes()
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/cs/inquiry")
                        .file(file1)
                        .file(file2)
                        .session(session)
                        .param("title", "첨부파일 테스트")
                        .param("category", "PROPOSAL")
                        .param("content", "첨부파일 포함 문의 내용"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/cs"));
    }

    @Test
    @DisplayName("POST /cs/inquiry - 유효성 검사 실패")
    void createInquiry_validationFailed() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.multipart("/cs/inquiry")
                        .session(session)
                        .param("title", "")
                        .param("category", "COMPLAINT")
                        .param("content", "내용"))
                .andExpect(status().isOk())
                .andExpect(view().name("inquiry-form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("inquiryCreateRequest", "title"));
    }

    @Test
    @DisplayName("POST /cs/inquiry - 파일 타입 예외 발생")
    void createInquiry_invalidFileType() throws Exception {
        doThrow(new InvalidFileTypeException("이미지 파일(GIF, JPG, PNG)만 업로드 가능합니다."))
                .when(inquiryService).createInquiry(eq(testCustomer), any(InquiryCreateRequest.class), anyList());

        MockMultipartFile invalidFile = new MockMultipartFile(
                "files",
                "document.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "This is a text file.".getBytes()
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/cs/inquiry")
                        .file(invalidFile)
                        .session(session)
                        .param("title", "잘못된 파일 타입")
                        .param("category", "OTHER")
                        .param("content", "텍스트 파일을 첨부"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/cs/inquiry"))
                .andExpect(flash().attributeExists("errorMessage"))
                .andExpect(flash().attribute("errorMessage", "이미지 파일(GIF, JPG, PNG)만 업로드 가능합니다."));
    }

    @Test
    @DisplayName("POST /cs/inquiry - 서비스 예외 발생 (RuntimeException)")
    void createInquiry_serviceRuntimeException() throws Exception {
        when(inquiryService.createInquiry(eq(testCustomer), any(InquiryCreateRequest.class), anyList()))
                .thenThrow(new RuntimeException("파일 저장 중 오류가 발생했습니다."));

        MockMultipartFile file = new MockMultipartFile("files", "good.png", MediaType.IMAGE_PNG_VALUE, "good".getBytes());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/cs/inquiry")
                        .file(file)
                        .session(session)
                        .param("title", "서비스 에러 테스트")
                        .param("category", "COMPLAINT")
                        .param("content", "내용"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/cs/inquiry"))
                .andExpect(flash().attributeExists("errorMessage"))
                .andExpect(flash().attribute("errorMessage", "파일 저장 중 오류가 발생했습니다."));
    }

    @Test
    @DisplayName("POST /cs/inquiry - 서비스 예외 발생 (일반 Exception)")
    void createInquiry_serviceGeneralException() throws Exception {
        when(inquiryService.createInquiry(eq(testCustomer), any(InquiryCreateRequest.class), anyList()))
                .thenReturn(null);

        MockMultipartFile file = new MockMultipartFile("files", "another.png", MediaType.IMAGE_PNG_VALUE, "another".getBytes());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/cs/inquiry")
                        .file(file)
                        .session(session)
                        .param("title", "일반 에러 테스트")
                        .param("category", "REFUND_EXCHANGE")
                        .param("content", "내용"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/cs/inquiry"))
                .andExpect(flash().attributeExists("errorMessage"))
                .andExpect(flash().attribute("errorMessage", "문의 등록 중 오류가 발생했습니다."));
    }

    @Test
    @DisplayName("GET /cs/inquiry/{id} - 문의 상세 조회 성공")
    void viewInquiryDetail_success() throws Exception {
        InquiryDetailResponse detail = InquiryDetailResponse.builder()
                .inquiryId(1L)
                .title("테스트")
                .answered(false)
                .attachments(Collections.emptyList())
                .build();

        when(inquiryService.getInquiryDetail(1L, testCustomer)).thenReturn(detail);

        mockMvc.perform(MockMvcRequestBuilders.get("/cs/inquiry/1").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("inquiry-detail"))
                .andExpect(model().attribute("inquiry", detail));
    }

    @Test
    @DisplayName("GET /cs/inquiry/{id} - 문의 없음 (404 via GlobalExceptionHandler)")
    void viewInquiryDetail_notFound() throws Exception {
        when(inquiryService.getInquiryDetail(99L, testCustomer))
                .thenThrow(new InquiryNotFoundException("해당 문의를 찾을 수 없습니다. ID: 99"));

        mockMvc.perform(MockMvcRequestBuilders.get("/cs/inquiry/99").session(session))
                .andExpect(status().isInternalServerError())
                .andExpect(view().name("error"))
                .andExpect(model().attributeExists("exception"));
    }

    @Test
    @DisplayName("GET /cs/inquiry/{id} - 접근 권한 없음 (타인 글 via GlobalExceptionHandler)")
    void viewInquiryDetail_accessDenied() throws Exception {
        when(inquiryService.getInquiryDetail(2L, testCustomer))
                .thenThrow(new InquiryAccessDeniedException("본인의 문의만 조회할 수 있습니다."));

        mockMvc.perform(MockMvcRequestBuilders.get("/cs/inquiry/2").session(session))
                .andExpect(status().isInternalServerError())
                .andExpect(view().name("error"))
                .andExpect(model().attributeExists("exception"));
    }
}
