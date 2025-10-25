package com.nhnacademy.nhnmartcs.inquiry;

import com.nhnacademy.nhnmartcs.global.exception.InquiryNotFoundException;
import com.nhnacademy.nhnmartcs.inquiry.controller.AdminController;
import com.nhnacademy.nhnmartcs.inquiry.dto.response.AdminInquirySummaryResponse;
import com.nhnacademy.nhnmartcs.inquiry.dto.response.InquiryDetailResponse;
import com.nhnacademy.nhnmartcs.inquiry.service.InquiryService;
import com.nhnacademy.nhnmartcs.user.domain.CSAdmin;
import com.nhnacademy.nhnmartcs.user.domain.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InquiryService inquiryService;

    private CSAdmin testAdmin;
    private Customer testCustomer;
    private MockHttpSession adminSession;
    private MockHttpSession customerSession;

    @BeforeEach
    void setUp() {
        testAdmin = new CSAdmin();
        testAdmin.setUserId(99L);
        testAdmin.setLoginId("admin");
        testAdmin.setName("관리자");

        testCustomer = new Customer();
        testCustomer.setUserId(1L);
        testCustomer.setLoginId("customer");
        testCustomer.setName("고객");

        adminSession = new MockHttpSession();
        adminSession.setAttribute("loginUser", testAdmin);

        customerSession = new MockHttpSession();
        customerSession.setAttribute("loginUser", testCustomer);
    }

    @Test
    @DisplayName("GET /cs/admin - 관리자 대시보드 (관리자 로그인)")
    void viewAdminDashboard_asAdmin() throws Exception {
        AdminInquirySummaryResponse summary = AdminInquirySummaryResponse.builder()
                .id(1L).title("미답변 문의").authorName("고객1").build();

        when(inquiryService.getUnansweredInquiries()).thenReturn(List.of(summary));

        mockMvc.perform(get("/cs/admin").session(adminSession))
                .andExpect(status().isOk())
                .andExpect(view().name("admin"))
                .andExpect(model().attributeExists("inquiries"));
    }

    @Test
    @DisplayName("GET /cs/admin - 접근 실패 (고객 로그인)")
    void viewAdminDashboard_asCustomer() throws Exception {
        mockMvc.perform(get("/cs/admin").session(customerSession))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/cs/login"));
    }

    @Test
    @DisplayName("GET /cs/admin - 접근 실패 (로그아웃)")
    void viewAdminDashboard_loggedOut() throws Exception {
        mockMvc.perform(get("/cs/admin"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/cs/login"));
    }

    @Test
    @DisplayName("GET /cs/admin/answer - 답변 폼 (관리자 로그인)")
    void answerForm_asAdmin() throws Exception {
        InquiryDetailResponse detail = InquiryDetailResponse.builder()
                .inquiryId(1L).title("문의").build();

        when(inquiryService.getInquiryDetailForAdmin(1L)).thenReturn(detail);

        mockMvc.perform(get("/cs/admin/answer").param("inquiryId", "1").session(adminSession))
                .andExpect(status().isOk())
                .andExpect(view().name("answer-form"))
                .andExpect(model().attribute("inquiry", detail))
                .andExpect(model().attributeExists("answerRequest"));
    }

    @Test
    @DisplayName("GET /cs/admin/answer - 접근 실패 (고객 로그인)")
    void answerForm_asCustomer() throws Exception {
        mockMvc.perform(get("/cs/admin/answer").param("inquiryId", "1").session(customerSession))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/cs/login"));
    }

    @Test
    @DisplayName("POST /cs/admin/answer - 답변 등록 성공")
    void addAnswer_success() throws Exception {
        mockMvc.perform(post("/cs/admin/answer").session(adminSession)
                        .param("inquiryId", "1")
                        .param("content", "답변입니다."))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/cs/admin"));

        verify(inquiryService).addAnswer(1L, "답변입니다.", testAdmin);
    }

    @Test
    @DisplayName("POST /cs/admin/answer - 유효성 검사 실패")
    void addAnswer_validationFailed() throws Exception {
        InquiryDetailResponse detail = InquiryDetailResponse.builder().inquiryId(1L).build();
        when(inquiryService.getInquiryDetailForAdmin(1L)).thenReturn(detail);

        mockMvc.perform(post("/cs/admin/answer").session(adminSession)
                        .param("inquiryId", "1")
                        .param("content", "")) // 내용 비어있음
                .andExpect(status().isOk())
                .andExpect(view().name("admin")) // [중요] 컨트롤러 로직이 validation 실패 시 "admin"으로 감
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("answerRequest", "content"));
    }

    @Test
    @DisplayName("POST /cs/admin/answer - 서비스 예외 발생")
    void addAnswer_serviceException() throws Exception {
        doThrow(new InquiryNotFoundException("Inquiry not found"))
                .when(inquiryService).addAnswer(eq(1L), anyString(), any(CSAdmin.class));

        mockMvc.perform(post("/cs/admin/answer").session(adminSession)
                        .param("inquiryId", "1")
                        .param("content", "답변입니다."))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/cs/admin/answer?inquiryId=1"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("POST /cs/admin/answer - 접근 실패 (고객 로그인)")
    void addAnswer_asCustomer() throws Exception {
        mockMvc.perform(post("/cs/admin/answer").session(customerSession)
                        .param("inquiryId", "1")
                        .param("content", "답변입니다."))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/cs/login"));
    }
}