package com.nhnacademy.nhnmartcs.inquiry;

import com.nhnacademy.nhnmartcs.global.exception.InquiryAccessDeniedException;
import com.nhnacademy.nhnmartcs.global.exception.InquiryNotFoundException;
import com.nhnacademy.nhnmartcs.inquiry.controller.CustomerController;
import com.nhnacademy.nhnmartcs.inquiry.dto.response.InquiryDetailResponse;
import com.nhnacademy.nhnmartcs.inquiry.dto.response.InquirySummaryResponse;
import com.nhnacademy.nhnmartcs.inquiry.service.InquiryService;
import com.nhnacademy.nhnmartcs.user.domain.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

        mockMvc.perform(get("/cs").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("inquiry-list"))
                .andExpect(model().attributeExists("inquiries"))
                .andExpect(model().attribute("inquiries", List.of(summary)));
    }

    @Test
    @DisplayName("GET /cs - 카테고리 필터링")
    void viewMyInquiries_withCategory() throws Exception {
        when(inquiryService.getMyInquiries(testCustomer, "COMPLAINT")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/cs").session(session).param("category", "COMPLAINT"))
                .andExpect(status().isOk())
                .andExpect(view().name("inquiry-list"))
                .andExpect(model().attribute("selectedCategory", "COMPLAINT"));
    }


    @Test
    @DisplayName("GET /cs - (로그아웃 상태, 인터셉터)")
    void viewMyInquiries_loggedOut() throws Exception {
        mockMvc.perform(get("/cs")) // 세션 없음
                .andExpect(status().isFound()) // 302 Redirect
                .andExpect(redirectedUrl("/cs/login"));
    }

    @Test
    @DisplayName("GET /cs/inquiry - 문의 작성 폼")
    void inquiryForm() throws Exception {
        mockMvc.perform(get("/cs/inquiry").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("inquiry-form"))
                .andExpect(model().attributeExists("inquiryCreateRequest"))
                .andExpect(model().attributeExists("categories"));
    }

    @Test
    @DisplayName("POST /cs/inquiry - 문의 생성 성공")
    void createInquiry_success() throws Exception {
        when(inquiryService.createInquiry(eq(testCustomer), any())).thenReturn(1L);

        mockMvc.perform(post("/cs/inquiry").session(session)
                        .param("title", "테스트 제목")
                        .param("category", "COMPLAINT")
                        .param("content", "테스트 내용입니다."))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/cs"));
    }

    @Test
    @DisplayName("POST /cs/inquiry - 유효성 검사 실패")
    void createInquiry_validationFailed() throws Exception {
        mockMvc.perform(post("/cs/inquiry").session(session)
                        .param("title", "") // 제목이 비어있음
                        .param("category", "COMPLAINT")
                        .param("content", "내용"))
                .andExpect(status().isOk())
                .andExpect(view().name("inquiry-form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("inquiryCreateRequest", "title"));
    }

    @Test
    @DisplayName("POST /cs/inquiry - 서비스 예외 발생")
    void createInquiry_serviceException() throws Exception {
        when(inquiryService.createInquiry(eq(testCustomer), any())).thenThrow(new RuntimeException("DB 오류"));

        mockMvc.perform(post("/cs/inquiry").session(session)
                        .param("title", "정상 제목")
                        .param("category", "COMPLAINT")
                        .param("content", "정상 내용"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/cs/inquiry"))
                .andExpect(flash().attributeExists("errorMessage"));
    }


    @Test
    @DisplayName("GET /cs/inquiry/{id} - 문의 상세 조회 성공")
    void viewInquiryDetail_success() throws Exception {
        InquiryDetailResponse detail = InquiryDetailResponse.builder()
                .inquiryId(1L).title("테스트").answered(false).build();

        when(inquiryService.getInquiryDetail(1L, testCustomer)).thenReturn(detail);

        mockMvc.perform(get("/cs/inquiry/1").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("inquiry-detail"))
                .andExpect(model().attribute("inquiry", detail));
    }

    @Test
    @DisplayName("GET /cs/inquiry/{id} - 문의 없음 (404)")
    void viewInquiryDetail_notFound() throws Exception {
        when(inquiryService.getInquiryDetail(99L, testCustomer))
                .thenThrow(new InquiryNotFoundException("해당 문의를 찾을 수 없습니다. ID: 99"));

        mockMvc.perform(get("/cs/inquiry/99").session(session))
                .andExpect(status().isInternalServerError()) // GlobalExceptionHandler
                .andExpect(view().name("error"))
                .andExpect(model().attributeExists("exception"));
    }

    @Test
    @DisplayName("GET /cs/inquiry/{id} - 접근 권한 없음 (타인 글)")
    void viewInquiryDetail_accessDenied() throws Exception {
        when(inquiryService.getInquiryDetail(2L, testCustomer))
                .thenThrow(new InquiryAccessDeniedException("본인의 문의만 조회할 수 있습니다."));

        mockMvc.perform(get("/cs/inquiry/2").session(session))
                .andExpect(status().isInternalServerError()) // GlobalExceptionHandler
                .andExpect(view().name("error"))
                .andExpect(model().attributeExists("exception"));
    }
}