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

    /**
     * Persist the given inquiry in the in-memory store, assigning a new unique ID if necessary.
     *
     * @param inquiry the inquiry to persist; if its `inquiryId` is null a new unique ID will be assigned
     * @return the persisted Inquiry, guaranteed to have a non-null `inquiryId`
     */
    @Override
    public Inquiry save(Inquiry inquiry) {
        if (inquiry.getInquiryId() == null) {
            inquiry.setInquiryId(sequence.incrementAndGet());
        }
        store.put(inquiry.getInquiryId(), inquiry);
        return inquiry;
    }

    /**
     * Retrieve an inquiry by its identifier.
     *
     * @param inquiryId the identifier of the inquiry to retrieve
     * @return an Optional containing the inquiry if present, empty otherwise
     */
    @Override
    public Optional<Inquiry> findById(Long inquiryId) {
        return Optional.ofNullable(store.get(inquiryId));
    }

    /**
     * Retrieve all inquiries belonging to the given customer, ordered by creation time newest first.
     *
     * @param customer the customer whose inquiries to retrieve (matched by the customer's `userId`)
     * @return a list of that customer's inquiries sorted by `createdAt` in descending order
     */
    @Override
    public List<Inquiry> findByCustomerOrderByCreatedAtDesc(Customer customer) {
        return store.values().stream()
                .filter(inquiry -> inquiry.getCustomer().getUserId().equals(customer.getUserId()))
                .sorted(Comparator.comparing(Inquiry::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Finds inquiries for the given customer in the specified category, ordered by creation time from newest to oldest.
     *
     * @param customer the customer whose inquiries to retrieve
     * @param category the inquiry category to filter by
     * @return the list of inquiries matching the customer and category, ordered by `createdAt` descending (newest first)
     */
    @Override
    public List<Inquiry> findByCustomerAndCategoryOrderByCreatedAtDesc(Customer customer, InquiryCategory category) {
        return store.values().stream()
                .filter(inquiry -> inquiry.getCustomer().getUserId().equals(customer.getUserId()) && inquiry.getCategory() == category)
                .sorted(Comparator.comparing(Inquiry::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all inquiries that have no answer, sorted by creation time in ascending order.
     *
     * @return a list of unanswered inquiries ordered from oldest to newest by creation time
     */
    @Override
    public List<Inquiry> findUnansweredInquiriesOrderByCreatedAtAsc() {
        return store.values().stream()
                .filter(inquiry -> inquiry.getAnswer() == null)
                .sorted(Comparator.comparing(Inquiry::getCreatedAt))
                .collect(Collectors.toList());
    }
}