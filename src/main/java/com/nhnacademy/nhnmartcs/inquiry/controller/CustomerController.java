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

    /**
     * Displays the logged-in customer's inquiries, optionally filtered by category, and prepares model attributes for the inquiry list view.
     *
     * Adds the following model attributes:
     * - "inquiries": list of InquirySummaryResponse for the customer
     * - "categories": all available InquiryCategory values
     * - "selectedCategory": the provided category filter (may be null)
     *
     * @param category optional inquiry category to filter the results; may be null to show all categories
     * @param session HTTP session used to retrieve the authenticated customer (expects "loginUser" attribute)
     * @param model model to populate with inquiry list data and categories
     * @return the view name "inquiry-list" when a customer is authenticated; otherwise a redirect to "/cs/login"
     */
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


    /**
     * Displays the inquiry creation form, ensuring a form backing object and category list are present in the model.
     *
     * @param model the MVC model to receive the form backing object ("inquiryCreateRequest") and the "categories" attribute
     * @return the view name for the inquiry creation form
     */
    @GetMapping("/inquiry")
    public String inquiryForm(Model model) {
        log.info("GET /cs/inquiry request received.");

        if (!model.containsAttribute("inquiryCreateRequest")) {
            model.addAttribute("inquiryCreateRequest", new InquiryCreateRequest());
        }
        model.addAttribute("categories", InquiryCategory.values());
        return "inquiry-form";
    }

    /**
     * Handle submission of a new inquiry form and create an inquiry for the logged-in customer.
     *
     * Performs validation of the provided request; if the user is not authenticated it redirects to the login page,
     * if validation fails it re-displays the inquiry form, and on service failure it sets a flash error message and
     * redirects back to the inquiry form. On successful creation it redirects to the customer inquiry list.
     *
     * @param inquiryCreateRequest the form data for creating an inquiry
     * @param bindingResult        validation results for the form data
     * @return                     the view name to render or redirect instruction:
     *                             redirects to "/cs/login" if unauthenticated, returns "inquiry-form" when validation fails,
     *                             redirects to "/cs/inquiry" on creation error, and redirects to "/cs" on success
     */
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

    /**
     * Displays the detail view for a customer's inquiry identified by the given id.
     *
     * If the customer is not logged in, the request will be redirected to the customer login page.
     * On success, the inquiry detail is added to the model under the attribute name "inquiry".
     *
     * @param id the identifier of the inquiry to display
     * @return the view name for the inquiry detail page or a redirect to the login page
     */
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
