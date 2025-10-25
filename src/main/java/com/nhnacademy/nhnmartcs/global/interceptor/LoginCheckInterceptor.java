package com.nhnacademy.nhnmartcs.global.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j; // Slf4j import
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j // Slf4j 로거 사용
public class LoginCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI(); // 요청 URI 확인
        log.info("LoginCheckInterceptor executing for URI: {}", requestURI); // 로그 추가

        HttpSession session = request.getSession(false); // 세션이 없으면 null 반환

        if (session == null || session.getAttribute("loginUser") == null) {
            log.warn("No active session found for URI: {}. Redirecting to /cs/login", requestURI); // 로그 추가
            response.sendRedirect("/cs/login"); // 리다이렉트 경로 확인
            return false; // 요청 처리 중단
        }

        log.info("Session found for URI: {}. Allowing access.", requestURI); // 로그 추가
        return true; // 요청 처리 계속 진행
    }
}
