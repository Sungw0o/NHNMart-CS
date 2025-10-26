package com.nhnacademy.nhnmartcs.inquiry.controller;

import com.nhnacademy.nhnmartcs.global.exception.InquiryAccessDeniedException;
import com.nhnacademy.nhnmartcs.global.exception.InquiryNotFoundException;
import com.nhnacademy.nhnmartcs.global.exception.InvalidFileTypeException;
import com.nhnacademy.nhnmartcs.inquiry.domain.InquiryCategory;
import com.nhnacademy.nhnmartcs.inquiry.dto.request.InquiryCreateRequest;
import com.nhnacademy.nhnmartcs.inquiry.dto.response.InquiryDetailResponse;
import com.nhnacademy.nhnmartcs.inquiry.dto.response.InquirySummaryResponse;
import com.nhnacademy.nhnmartcs.inquiry.service.InquiryService;
import com.nhnacademy.nhnmartcs.user.domain.Customer;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;

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

        return "inquiry-list";
    }


    @GetMapping("/inquiry")
    public String inquiryForm(Model model) {

        if (!model.containsAttribute("inquiryCreateRequest")) {
            model.addAttribute("inquiryCreateRequest", new InquiryCreateRequest());
        }
        model.addAttribute("categories", InquiryCategory.values());
        return "inquiry-form";
    }

    @PostMapping("/inquiry")
    public String createInquiry(@Valid @ModelAttribute InquiryCreateRequest inquiryCreateRequest,
                                BindingResult bindingResult,
                                @RequestParam(name = "files", required = false) List<MultipartFile> files,
                                HttpSession session,
                                RedirectAttributes redirectAttributes,
                                Model model) {

        Customer customer = (Customer) session.getAttribute("loginUser");
        if (customer == null) {
            return "redirect:/cs/login";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", InquiryCategory.values());
            return "inquiry-form";
        }

        Long inquiryId = null;
        try {
            List<MultipartFile> actualFiles = files != null ? files : Collections.emptyList();
            inquiryId = inquiryService.createInquiry(customer, inquiryCreateRequest, actualFiles);

        } catch (InvalidFileTypeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("inquiryCreateRequest", inquiryCreateRequest);
            return "redirect:/cs/inquiry";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("inquiryCreateRequest", inquiryCreateRequest);
            return "redirect:/cs/inquiry";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "문의 등록 중 예상치 못한 오류가 발생했습니다.");
            redirectAttributes.addFlashAttribute("inquiryCreateRequest", inquiryCreateRequest);
            return "redirect:/cs/inquiry";
        }

        if (inquiryId == null) {
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
        try {
            InquiryDetailResponse inquiryDetail = inquiryService.getInquiryDetail(id, customer);
            model.addAttribute("inquiry", inquiryDetail);
            return "inquiry-detail";
        } catch (InquiryNotFoundException | InquiryAccessDeniedException e) {
            throw e;
        }
    }
}