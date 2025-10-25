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

    /**
     * Ensures the request has an authenticated user and enforces admin-only access for "/cs/admin" URIs.
     *
     * If no session or no "loginUser" attribute is present, the method sends a redirect to "/cs/login" and halts processing.
     * For requests with a URI beginning with "/cs/admin", only instances of CSAdmin are allowed; non-admins are redirected to "/cs/login".
     *
     * @param request  the current HTTP request
     * @param response the current HTTP response; may be used to send a redirect to "/cs/login"
     * @param handler  the chosen handler to execute (not used by this interceptor)
     * @return true if request processing should continue, false if processing is halted (redirect performed)
     */
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