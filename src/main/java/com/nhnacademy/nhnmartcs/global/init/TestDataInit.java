package com.nhnacademy.nhnmartcs.global.init;

import com.nhnacademy.nhnmartcs.user.domain.CSAdmin;
import com.nhnacademy.nhnmartcs.user.domain.Customer;
import com.nhnacademy.nhnmartcs.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Slf4j
@Component
@RequiredArgsConstructor
public class TestDataInit {

    private final UserRepository userRepository;

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
        log.info("테스트 데이터 주입 성공");
    }
}