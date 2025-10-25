package com.nhnacademy.nhnmartcs.inquiry.service;

import com.nhnacademy.nhnmartcs.inquiry.dto.request.InquiryCreateRequest;
import com.nhnacademy.nhnmartcs.inquiry.dto.response.AdminInquirySummaryResponse;
import com.nhnacademy.nhnmartcs.inquiry.dto.response.InquiryDetailResponse;
import com.nhnacademy.nhnmartcs.inquiry.dto.response.InquirySummaryResponse;
import com.nhnacademy.nhnmartcs.user.domain.CSAdmin;
import com.nhnacademy.nhnmartcs.user.domain.Customer;

import java.util.List;

public interface InquiryService {

    /**
 * Creates a new inquiry associated with the given customer.
 *
 * @param customer   the customer submitting the inquiry
 * @param requestDto the data for the inquiry to create
 * @return the identifier of the created inquiry
 */
Long createInquiry(Customer customer, InquiryCreateRequest requestDto);
    /**
 * Retrieves the specified customer's inquiries, optionally filtered by category.
 *
 * @param customer the customer whose inquiries to retrieve
 * @param category the category to filter inquiries by; may be null or empty to retrieve all categories
 * @return a list of inquiry summary responses for the customer; empty list if none are found
 */
List<InquirySummaryResponse> getMyInquiries(Customer customer, String category);
    /**
 * Retrieve detailed information for a specific inquiry that belongs to the provided customer.
 *
 * @param inquiryId the identifier of the inquiry to retrieve
 * @param customer the owner of the inquiry
 * @return the inquiry details as an InquiryDetailResponse
 */
InquiryDetailResponse getInquiryDetail(Long inquiryId, Customer customer);

    /**
 * Retrieves a list of inquiries that have not yet received an answer for administrative review.
 *
 * @return a list of AdminInquirySummaryResponse objects representing unanswered inquiries
 */
List<AdminInquirySummaryResponse> getUnansweredInquiries();
    /**
 * Fetches detailed information for a specific inquiry for administrative review.
 *
 * @param inquiryId the identifier of the inquiry to retrieve
 * @return an InquiryDetailResponse containing full details of the specified inquiry for administrative review
 */
InquiryDetailResponse getInquiryDetailForAdmin(Long inquiryId);
    /**
 * Adds an answer authored by the given admin to the inquiry identified by inquiryId.
 *
 * @param inquiryId    the identifier of the inquiry to answer
 * @param answerContent the text content of the answer
 * @param admin        the CSAdmin who authors the answer
 */
void addAnswer(Long inquiryId, String answerContent, CSAdmin admin);
}