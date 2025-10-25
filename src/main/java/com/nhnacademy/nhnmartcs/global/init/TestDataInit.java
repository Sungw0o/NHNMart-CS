package com.nhnacademy.nhnmartcs.global.init;

import com.nhnacademy.nhnmartcs.inquiry.domain.Inquiry;
import com.nhnacademy.nhnmartcs.inquiry.domain.InquiryCategory;
import com.nhnacademy.nhnmartcs.inquiry.repository.InquiryRepository;
import com.nhnacademy.nhnmartcs.user.domain.CSAdmin;
import com.nhnacademy.nhnmartcs.user.domain.Customer;
import com.nhnacademy.nhnmartcs.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class TestDataInit {

    private final UserRepository userRepository;
    private final InquiryRepository inquiryRepository;

    @PostConstruct
    public void init() {
        log.info("테스트 데이터 주입 시작");
        Customer customer = new Customer();
        customer.setLoginId("c1234");
        customer.setPassword("1234");
        customer.setName("고객1");
        userRepository.save(customer);

        CSAdmin admin = new CSAdmin();
        admin.setLoginId("a1234");
        admin.setPassword("1234");
        admin.setName("관리자1");
        userRepository.save(admin);

        Inquiry testInquiry = new Inquiry(
                null,
                "배송이 너무 늦어요",
                "상품이 3일째 오지 않고 있습니다. 확인 부탁드립니다.",
                InquiryCategory.COMPLAINT,
                LocalDateTime.now().minusDays(1), 
                customer,
                null,
                Collections.emptyList()
        );
        inquiryRepository.save(testInquiry);
        log.info("테스트 데이터 주입 성공");
    }
}