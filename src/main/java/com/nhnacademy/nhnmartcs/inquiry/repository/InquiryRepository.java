package com.nhnacademy.nhnmartcs.inquiry.repository;

import com.nhnacademy.nhnmartcs.inquiry.domain.Inquiry;
import com.nhnacademy.nhnmartcs.inquiry.domain.InquiryCategory;
import com.nhnacademy.nhnmartcs.user.domain.Customer;

import java.util.List;
import java.util.Optional;

public interface InquiryRepository {

    Inquiry save(Inquiry inquiry);
    Optional<Inquiry> findById(Long inquiryId);
    List<Inquiry> findByCustomerOrderByCreatedAtDesc(Customer customer);
    List<Inquiry> findByCustomerAndCategoryOrderByCreatedAtDesc(Customer customer, InquiryCategory category);
    List<Inquiry> findUnansweredInquiriesOrderByCreatedAtAsc();
}
