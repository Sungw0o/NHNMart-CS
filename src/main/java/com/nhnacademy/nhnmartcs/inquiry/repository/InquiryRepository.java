package com.nhnacademy.nhnmartcs.inquiry.repository;

import com.nhnacademy.nhnmartcs.inquiry.domain.Inquiry;
import com.nhnacademy.nhnmartcs.inquiry.domain.InquiryCategory;
import com.nhnacademy.nhnmartcs.user.domain.Customer;

import java.util.List;
import java.util.Optional;

public interface InquiryRepository {

    /**
 * Persists the given inquiry and returns the saved instance.
 *
 * @param inquiry the Inquiry to persist
 * @return the persisted Inquiry with any generated identifiers or audit fields populated
 */
Inquiry save(Inquiry inquiry);
    /**
 * Retrieve an inquiry by its identifier.
 *
 * @param inquiryId the identifier of the inquiry to retrieve
 * @return an Optional containing the Inquiry if found, or empty if none is found
 */
Optional<Inquiry> findById(Long inquiryId);
    /**
 * Retrieve all inquiries for the given customer ordered by creation time descending.
 *
 * @param customer the customer whose inquiries are requested
 * @return a list of the customer's inquiries ordered from newest to oldest
 */
List<Inquiry> findByCustomerOrderByCreatedAtDesc(Customer customer);
    /**
 * Retrieve inquiries for a specific customer filtered by the given category, ordered by creation time descending.
 *
 * @param customer the customer whose inquiries to retrieve
 * @param category the category to filter inquiries by
 * @return a list of matching Inquiry objects ordered by `createdAt` from newest to oldest; empty if none found
 */
List<Inquiry> findByCustomerAndCategoryOrderByCreatedAtDesc(Customer customer, InquiryCategory category);
    /**
 * Retrieve all inquiries that have not been answered, ordered by creation time ascending.
 *
 * @return a list of Inquiry objects representing unanswered inquiries ordered by createdAt in ascending order; an empty list if none are found
 */
List<Inquiry> findUnansweredInquiriesOrderByCreatedAtAsc();
}