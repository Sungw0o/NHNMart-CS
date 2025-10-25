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

    @GetMapping("/cs/login")
    public String loginForm(){

        return "login";
    }


    /**
     * Authenticate credentials, store the authenticated user in the HTTP session, and redirect according to the user's role.
     *
     * @param loginRequest object containing the login identifier and password used to authenticate the user
     * @return `"redirect:/cs"` if the authenticated user is a Customer, `"redirect:/cs/admin"` if the authenticated user is a CSAdmin, otherwise `"redirect:/"`
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
            return "redirect:/cs";
        } else if (loginUser instanceof CSAdmin) {
            return "redirect:/cs/admin";
        }
        return "redirect:/";
    }

    @GetMapping("/cs/logout")
    public String doLogout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        return "redirect:/cs/login";
    }

}