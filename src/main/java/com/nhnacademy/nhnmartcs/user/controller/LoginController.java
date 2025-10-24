package com.nhnacademy.nhnmartcs.user.controller;

import com.nhnacademy.nhnmartcs.user.domain.CSAdmin;
import com.nhnacademy.nhnmartcs.user.domain.Customer;
import com.nhnacademy.nhnmartcs.user.domain.User;
import com.nhnacademy.nhnmartcs.user.dto.LoginRequest;
import com.nhnacademy.nhnmartcs.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LoginController {

    private final UserService userService;

    /**
     * Shows the login page.
     *
     * @return the view name "login"
     */
    @GetMapping("/cs/login")
    public String loginForm(){

        return "login";
    }


    /**
     * Authenticates a user, stores the authenticated user in the HTTP session under the attribute
     * "loginUser", and selects the post-login view.
     *
     * @param loginRequest contains the credentials (`loginId` and `password`) used for authentication
     * @param request the servlet request used to obtain or create the HTTP session
     * @return "board" when the authenticated user is a Customer, "admin" when the authenticated user is a CSAdmin, "redirect:/" otherwise
     */
    @PostMapping("/cs/login")
    public String doLogin(LoginRequest loginRequest, HttpServletRequest request){
        User loginUser = userService.doLogin(
                loginRequest.getLoginId(),
                loginRequest.getPassword()
        );

        HttpSession session = request.getSession(true);
        session.setAttribute("loginUser", loginUser);

        if (loginUser instanceof Customer) {
            return "board";
        } else if (loginUser instanceof CSAdmin) {
            return "admin";
        }
        return "redirect:/";
    }

    /**
     * Invalidates the current HTTP session if one exists and redirects the client to the login page.
     *
     * @return the redirect target for the login page ("redirect:/cs/login")
     */
    @GetMapping("/cs/logout")
    public String doLogout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        return "redirect:/cs/login";
    }

}