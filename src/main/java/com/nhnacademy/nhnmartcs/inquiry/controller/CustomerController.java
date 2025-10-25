package com.nhnacademy.nhnmartcs.inquiry.controller;

import com.nhnacademy.nhnmartcs.inquiry.domain.InquiryCategory;
import com.nhnacademy.nhnmartcs.inquiry.dto.request.InquiryCreateRequest;
import com.nhnacademy.nhnmartcs.inquiry.dto.response.InquiryDetailResponse;
import com.nhnacademy.nhnmartcs.inquiry.dto.response.InquirySummaryResponse;
import com.nhnacademy.nhnmartcs.inquiry.service.InquiryService;
import com.nhnacademy.nhnmartcs.user.domain.Customer;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
        log.info("GET /cs request received. Category: {}", category);
        Customer customer = (Customer) session.getAttribute("loginUser");

        if (customer == null) {
            return "redirect:/cs/login";
        }

        List<InquirySummaryResponse> inquiries = inquiryService.getMyInquiries(customer, category);

        model.addAttribute("inquiries", inquiries);
        model.addAttribute("categories", InquiryCategory.values());
        model.addAttribute("selectedCategory", category);
        log.info("Showing {} inquiries for customer ID: {}", inquiries.size(), customer.getUserId());


        return "inquiry-list";
    }


    @GetMapping("/inquiry")
    public String inquiryForm(Model model) {
        log.info("GET /cs/inquiry request received.");

        if (!model.containsAttribute("inquiryCreateRequest")) {
            model.addAttribute("inquiryCreateRequest", new InquiryCreateRequest());
        }
        model.addAttribute("categories", InquiryCategory.values());
        return "inquiry-form";
    }

    @PostMapping("/inquiry")
    public String createInquiry(@Valid @ModelAttribute InquiryCreateRequest inquiryCreateRequest,
                                BindingResult bindingResult,
                                // @RequestParam("files") List<MultipartFile> files, // 파일 첨부 시 주석 해제
                                HttpSession session,
                                RedirectAttributes redirectAttributes,
                                Model model) {

        log.info("POST /cs/inquiry request received. Title: {}", inquiryCreateRequest.getTitle());
        Customer customer = (Customer) session.getAttribute("loginUser");
        if (customer == null) {
            return "redirect:/cs/login";
        }

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors occurred: {}", bindingResult.getAllErrors());
            model.addAttribute("categories", InquiryCategory.values());
            return "inquiry-form";
        }

        // 파일 첨부 로직은 주석 처리
        // try {
        //     List<Attachment> savedAttachments = attachmentService.saveFiles(files);
        // } catch (InvalidFileTypeException e) {
        //     log.warn("Invalid file type uploaded: {}", e.getMessage());
        //     redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        //     redirectAttributes.addFlashAttribute("inquiryCreateRequest", inquiryCreateRequest); // 입력값 유지
        //     return "redirect:/cs/inquiry";
        // }

        try {
            // 서비스를 호출하여 문의 생성 (파일 첨부 제외 버전)
            Long inquiryId = inquiryService.createInquiry(customer, inquiryCreateRequest /*, savedAttachments */);
            log.info("Inquiry created successfully. ID: {}", inquiryId);
        } catch (Exception e) {
            log.error("Error creating inquiry: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "문의 등록 중 오류가 발생했습니다. 다시 시도해주세요.");

            return "redirect:/cs/inquiry";
        }

        return "redirect:/cs";
    }

    @GetMapping("/inquiry/{id}")
    public String viewInquiryDetail(@PathVariable Long id,
                                    HttpSession session,
                                    Model model) {
        log.info("GET /cs/inquiry/{} request received.", id);
        Customer customer = (Customer) session.getAttribute("loginUser");
        if (customer == null) {
            return "redirect:/cs/login";
        }
        InquiryDetailResponse inquiryDetail = inquiryService.getInquiryDetail(id, customer);
        model.addAttribute("inquiry", inquiryDetail);
        return "inquiry-detail";
    }

}

