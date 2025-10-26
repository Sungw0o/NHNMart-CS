package com.nhnacademy.nhnmartcs.inquiry.controller;


import com.nhnacademy.nhnmartcs.inquiry.dto.request.AnswerCreateRequest;
import com.nhnacademy.nhnmartcs.inquiry.dto.response.AdminInquirySummaryResponse;
import com.nhnacademy.nhnmartcs.inquiry.dto.response.InquiryDetailResponse;
import com.nhnacademy.nhnmartcs.inquiry.service.InquiryService;
import com.nhnacademy.nhnmartcs.user.domain.CSAdmin;
import com.nhnacademy.nhnmartcs.user.domain.User;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/cs/admin")
public class AdminController {

    private final InquiryService inquiryService;

    @GetMapping
    public String viewAdminDashboard(HttpSession session, Model model) {
        log.info("GET /cs/admin request received.");
        User loginUser = (User) session.getAttribute("loginUser");

        if (loginUser == null || !(loginUser instanceof CSAdmin)) {
            log.warn("Non-admin user attempted to access /cs/admin. Redirecting to login.");
            return "redirect:/cs/login";
        }

        List<AdminInquirySummaryResponse> inquiries = inquiryService.getUnansweredInquiries();

        model.addAttribute("inquiries", inquiries);
        log.info("Showing {} unanswered inquiries for admin.", inquiries.size());

        return "admin";
    }

    @GetMapping("/answer")
    public String answerForm(@RequestParam("inquiryId") Long inquiryId,
                             HttpSession session,
                             Model model) {

        log.info("GET /cs/admin/answer form for inquiryId: {}", inquiryId);
        User loginUser = (User) session.getAttribute("loginUser");

        if (loginUser == null || !(loginUser instanceof CSAdmin)) {
            return "redirect:/cs/login";
        }

        InquiryDetailResponse inquiry = inquiryService.getInquiryDetailForAdmin(inquiryId);
        model.addAttribute("inquiry", inquiry);

        if (!model.containsAttribute("answerRequest")) {
            model.addAttribute("answerRequest", new AnswerCreateRequest());
        }

        return "answer-form";
    }

    @PostMapping("/answer")
    public String addAnswer(@RequestParam("inquiryId") Long inquiryId,
                            @Valid @ModelAttribute("answerRequest") AnswerCreateRequest answerRequest,
                            BindingResult bindingResult,
                            HttpSession session,
                            Model model,
                            RedirectAttributes redirectAttributes) {

        log.info("POST /cs/admin/answer for inquiryId: {}", inquiryId);
        CSAdmin admin = (CSAdmin) session.getAttribute("loginUser");

        if (admin == null) {
            return "redirect:/cs/login";
        }

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors while submitting answer: {}", bindingResult.getAllErrors());
            InquiryDetailResponse inquiry = inquiryService.getInquiryDetailForAdmin(inquiryId);
            model.addAttribute("inquiry", inquiry);
            return "admin";
        }

        try {
            inquiryService.addAnswer(inquiryId, answerRequest.getContent(), admin);
            log.info("Answer added successfully for inquiryId: {}", inquiryId);
        } catch (Exception e) {
            log.error("Error adding answer: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "답변 등록 중 오류가 발생했습니다.");
            return "redirect:/cs/admin/answer?inquiryId=" + inquiryId;
        }

        return "redirect:/cs/admin";
    }
}