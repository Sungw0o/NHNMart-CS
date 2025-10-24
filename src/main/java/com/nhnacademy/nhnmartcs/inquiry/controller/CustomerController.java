package com.nhnacademy.nhnmartcs.inquiry.controller;

import com.nhnacademy.nhnmartcs.inquiry.domain.InquiryCategory;
import com.nhnacademy.nhnmartcs.inquiry.dto.InquiryCreateRequest;
import com.nhnacademy.nhnmartcs.inquiry.dto.InquiryDetailResponse;
import com.nhnacademy.nhnmartcs.inquiry.dto.InquirySummaryResponse;
import com.nhnacademy.nhnmartcs.inquiry.service.InquiryService;
import com.nhnacademy.nhnmartcs.user.domain.Customer;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/cs")
public class CustomerController {

    private final InquiryService inquiryService;

    @GetMapping
    public String viewMyInquiries(@RequestParam(required = false) String category,
                                  HttpSession session,
                                  Model model) {
        Customer customer = (Customer) session.getAttribute("loginUser");
        if (customer == null) {
            return "redirect:/cs/login";
        }

        List<InquirySummaryResponse> inquiries = inquiryService.getMyInquiries(customer, category);

        model.addAttribute("inquiries", inquiries);
        model.addAttribute("categories", InquiryCategory.values());
        model.addAttribute("selectedCategory", category);
        log.info("Showing {} inquiries for customer ID: {}", inquiries.size(), customer.getUserId());
        return "board";
    }

    @GetMapping("/inquiry")
    public String inquiryForm(Model model) {
        if (!model.containsAttribute("inquiryCreateRequest")) {
            model.addAttribute("inquiryCreateRequest", new InquiryCreateRequest());
        }
        model.addAttribute("categories", InquiryCategory.values());
        return "inquiry-form.html";
    }

    @PostMapping("/inquiry")
    public String createInquiry(@Valid @ModelAttribute InquiryCreateRequest inquiryCreateRequest,
                                BindingResult bindingResult,
                                // @RequestParam("files") List<MultipartFile> files, // 파일 첨부 시 주석 해제
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors occurred: {}", bindingResult.getAllErrors());
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.inquiryCreateRequest", bindingResult);
            redirectAttributes.addFlashAttribute("inquiryCreateRequest", inquiryCreateRequest);
            return "redirect:/cs/inquiry";
        }

        Customer customer = (Customer) session.getAttribute("loginUser");
        if (customer == null) {
            return "redirect:/cs/login";
        }

        // 3. 파일 처리 (지금은 비워둠)
        // TODO: 파일 처리 로직 (AttachmentService 호출)

        try {
            Long inquiryId = inquiryService.createInquiry(customer, inquiryCreateRequest);
            log.info("Inquiry created successfully. ID: {}", inquiryId);
        } catch (Exception e) {
            log.error("Error creating inquiry: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "문의 등록 중 오류가 발생했습니다.");
            redirectAttributes.addFlashAttribute("inquiryCreateRequest", inquiryCreateRequest);
            return "redirect:/cs/inquiry";
        }

        return "redirect:/cs";
    }

    @GetMapping("/inquiry/{id}")
    public String viewInquiryDetail(@PathVariable Long id,
                                    HttpSession session,
                                    Model model) {
        Customer customer = (Customer) session.getAttribute("loginUser");
        if (customer == null) {
            return "redirect:/cs/login";
        }

        InquiryDetailResponse inquiryDetail = inquiryService.getInquiryDetail(id, customer);

        model.addAttribute("inquiry", inquiryDetail);
        return "inquiry-detail";
    }

}

