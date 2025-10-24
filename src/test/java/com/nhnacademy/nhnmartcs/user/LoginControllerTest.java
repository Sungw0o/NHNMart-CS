package com.nhnacademy.nhnmartcs.user.controller;

import com.nhnacademy.nhnmartcs.global.exception.LoginFailedException;
import com.nhnacademy.nhnmartcs.user.domain.CSAdmin;
import com.nhnacademy.nhnmartcs.user.domain.Customer;
import com.nhnacademy.nhnmartcs.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoginController.class)
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private Customer testCustomer;
    private CSAdmin testAdmin;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setLoginId("customer1");
        testCustomer.setPassword("1234");
        testCustomer.setName("고객1");

        testAdmin = new CSAdmin();
        testAdmin.setLoginId("admin1");
        testAdmin.setPassword("1234");
        testAdmin.setName("관리자1");
    }

    @Test
    @DisplayName("로그인 폼 페이지 요청")
    void loginForm() throws Exception {
        mockMvc.perform(get("/cs/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    @DisplayName("고객 로그인 성공")
    void doLogin_Customer_Success() throws Exception {
        when(userService.doLogin("customer1", "1234")).thenReturn(testCustomer);

        mockMvc.perform(post("/cs/login")
                        .param("loginId", "customer1")
                        .param("password", "1234"))
                .andExpect(status().isOk())
                .andExpect(view().name("board"))
                .andExpect(request().sessionAttribute("loginUser", testCustomer));
    }

    @Test
    @DisplayName("관리자 로그인 성공")
    void doLogin_Admin_Success() throws Exception {
        when(userService.doLogin("admin1", "1234")).thenReturn(testAdmin);

        mockMvc.perform(post("/cs/login")
                        .param("loginId", "admin1")
                        .param("password", "1234"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin"))
                .andExpect(request().sessionAttribute("loginUser", testAdmin));
    }

    @Test
    @DisplayName("로그인 실패 - 아이디 없음")
    void doLogin_Fail_UserNotFound() throws Exception {

        String errorMessage = "아이디가 존재하지 않습니다.";
        when(userService.doLogin(anyString(), anyString()))
                .thenThrow(new LoginFailedException(errorMessage));


        mockMvc.perform(post("/cs/login")
                        .param("loginId", "wrongUser")
                        .param("password", "1234"))
                .andExpect(result -> assertInstanceOf(LoginFailedException.class, result.getResolvedException()))
                .andExpect(result -> assertThat(result.getResolvedException().getMessage()).isEqualTo(errorMessage));

    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void doLogin_Fail_PasswordMismatch() throws Exception {

        String errorMessage = "비밀번호가 일치하지 않습니다.";
        when(userService.doLogin("customer1", "wrongPassword"))
                .thenThrow(new LoginFailedException(errorMessage));

        mockMvc.perform(post("/cs/login")
                        .param("loginId", "customer1")
                        .param("password", "wrongPassword"))
                .andExpect(result -> assertInstanceOf(LoginFailedException.class, result.getResolvedException()))
                .andExpect(result -> assertThat(result.getResolvedException().getMessage()).isEqualTo(errorMessage));

    }

    @Test
    @DisplayName("로그아웃")
    void doLogout() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginUser", testCustomer);

        mockMvc.perform(get("/cs/logout").session(session))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/cs/login"))
                .andExpect(request().sessionAttributeDoesNotExist("loginUser"));
    }
}

