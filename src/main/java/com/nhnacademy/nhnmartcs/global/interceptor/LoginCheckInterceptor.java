package com.nhnacademy.nhnmartcs.global.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class LoginCheckInterceptor implements HandlerInterceptor {

    /**
     * Ensures the request is authenticated before allowing controller handling.
     *
     * Redirects unauthenticated requests to "/cs/login" and prevents the handler from executing.
     *
     * @param request  the current HTTP request
     * @param response the current HTTP response
     * @param handler  the chosen handler to execute, provided by the framework
     * @return `true` if the request should proceed to the handler, `false` if the request was redirected to the login page
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loginUser") == null) {
            response.sendRedirect("/cs/login");
            return false;
        }
        return true;
    }
}