package com.nhnacademy.nhnmartcs.inquiry.repository.impl;

import com.nhnacademy.nhnmartcs.inquiry.domain.Inquiry;
import com.nhnacademy.nhnmartcs.inquiry.domain.InquiryCategory;
import com.nhnacademy.nhnmartcs.inquiry.repository.InquiryRepository;
import com.nhnacademy.nhnmartcs.user.domain.Customer;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InquiryRepositoryImpl implements InquiryRepository {

    private static final Map<Long, Inquiry> store = new HashMap<>();
    private static final AtomicLong sequence = new AtomicLong(0L);

    @Override
    public Inquiry save(Inquiry inquiry) {
        if (inquiry.getInquiryId() == null) {
            inquiry.setInquiryId(sequence.incrementAndGet());
        }
        store.put(inquiry.getInquiryId(), inquiry);
        return inquiry;
    }

    @Override
    public Optional<Inquiry> findById(Long inquiryId) {
        return Optional.ofNullable(store.get(inquiryId));
    }

    @Override
    public List<Inquiry> findByCustomerOrderByCreatedAtDesc(Customer customer) {
        return store.values().stream()
                .filter(inquiry -> inquiry.getCustomer().getUserId().equals(customer.getUserId()))
                .sorted(Comparator.comparing(Inquiry::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Inquiry> findByCustomerAndCategoryOrderByCreatedAtDesc(Customer customer, InquiryCategory category) {
        return store.values().stream()
                .filter(inquiry -> inquiry.getCustomer().getUserId().equals(customer.getUserId()) && inquiry.getCategory() == category)
                .sorted(Comparator.comparing(Inquiry::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Inquiry> findUnansweredInquiriesOrderByCreatedAtAsc() {
        return store.values().stream()
                .filter(inquiry -> inquiry.getAnswer() == null)
                .sorted(Comparator.comparing(Inquiry::getCreatedAt))
                .collect(Collectors.toList());
    }
}
