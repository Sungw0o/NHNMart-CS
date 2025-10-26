package com.nhnacademy.nhnmartcs.global.interceptor;

import com.nhnacademy.nhnmartcs.user.domain.CSAdmin;
import com.nhnacademy.nhnmartcs.user.domain.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
public class LoginCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        log.info("LoginCheckInterceptor executing for URI: {}", requestURI);

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("loginUser") == null) {
            log.warn("No active session found for URI: {}. Redirecting to /cs/login", requestURI);
            response.sendRedirect("/cs/login");
            return false;
        }

        if (requestURI.startsWith("/cs/admin")) {
            User loginUser = (User) session.getAttribute("loginUser");
            if (!(loginUser instanceof CSAdmin)) {
                log.warn("Non-admin user ({}) attempted to access admin URI: {}. Redirecting to /cs/login",
                        loginUser.getClass().getSimpleName(), requestURI);
                response.sendRedirect("/cs/login");
                return false;
            }
            log.info("Admin user accessed admin URI: {}. Allowing access.", requestURI);
        } else {
            log.info("Session found for non-admin URI: {}. Allowing access.", requestURI);
        }

        return true;
    }
}