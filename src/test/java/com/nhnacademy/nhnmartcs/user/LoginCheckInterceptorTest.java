package com.nhnacademy.nhnmartcs.user;

import com.nhnacademy.nhnmartcs.global.config.WebMvcConfig;
import com.nhnacademy.nhnmartcs.global.interceptor.LoginCheckInterceptor;
import com.nhnacademy.nhnmartcs.user.domain.Customer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = LoginCheckInterceptorTest.TestController.class)
@Import({WebMvcConfig.class, LoginCheckInterceptor.class})
class LoginCheckInterceptorTest {

    @Autowired
    private MockMvc mockMvc;

    @Controller
    static class TestController {
        @GetMapping("/cs")
        public String protectedPage() {
            return "inquiry-form";
        }
    }

    @Test
    @DisplayName("로그인 안 한 상태로 보호된 경로(/cs) 접근 시 로그인 페이지로 리다이렉트")
    void preHandle_NoSession_RedirectsToLogin() throws Exception {
        mockMvc.perform(get("/cs"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/cs/login"));
    }

    @Test
    @DisplayName("로그인 한 상태로 보호된 경로(/cs) 접근 시 (컨트롤러 없으므로) 500 에러 발생")
    void preHandle_WithSession_AllowsAccessButNoControllerMapped() throws Exception {

        MockHttpSession session = new MockHttpSession();
        Customer loggedInUser = new Customer();
        session.setAttribute("loginUser", loggedInUser);

        mockMvc.perform(get("/cs").session(session))
                .andExpect(status().isInternalServerError())
                .andExpect(view().name("error"))
                .andExpect(result -> assertInstanceOf(NoResourceFoundException.class, result.getResolvedException()));
    }

    @Test
    @DisplayName("로그인 안 한 상태로 로그인 페이지(/cs/login) 접근 시 성공 (제외 경로)")
    void preHandle_AccessLoginPageWithoutSession_AllowsAccess() throws Exception {
        mockMvc.perform(get("/cs/login"))
                .andExpect(status().isOk());

    }

    @Test
    @DisplayName("로그인 안 한 상태로 CSS 파일 접근 시 성공 (제외 경로)")
    void preHandle_AccessCssWithoutSession_AllowsAccess() throws Exception {
        mockMvc.perform(get("/css/login.css"))
                .andExpect(status().isOk());

    }
}
