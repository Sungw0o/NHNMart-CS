package com.nhnacademy.nhnmartcs.user.controller;

import com.nhnacademy.nhnmartcs.user.domain.CSAdmin;
import com.nhnacademy.nhnmartcs.user.domain.Customer;
import com.nhnacademy.nhnmartcs.user.domain.User;
import com.nhnacademy.nhnmartcs.user.dto.LoginRequest;
import com.nhnacademy.nhnmartcs.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginForm(){
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(LoginRequest loginRequest, HttpServletRequest request){
        User loginUser = userService.doLogin(
                loginRequest.getLoginId(),
                loginRequest.getPassword()
        );

        HttpSession session = request.getSession(true);
        session.setAttribute("loginUser", loginUser);

        if (loginUser instanceof Customer) {
            return "redirect:/board";
        } else if (loginUser instanceof CSAdmin) {
            return "redirect:/admin";
        }

        return "redirect:/";
    }

    @GetMapping("/logout")
    public String doLogout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/login";
    }

}
