package com.nhnacademy.nhnmartcs.global.init;

import com.nhnacademy.nhnmartcs.inquiry.domain.Inquiry;
import com.nhnacademy.nhnmartcs.inquiry.domain.InquiryCategory;
import com.nhnacademy.nhnmartcs.inquiry.repository.InquiryRepository;
import com.nhnacademy.nhnmartcs.user.domain.CSAdmin;
import com.nhnacademy.nhnmartcs.user.domain.Customer;
import com.nhnacademy.nhnmartcs.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TestDataInit {

    private final UserRepository userRepository;
    private final InquiryRepository inquiryRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

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

        String originalFilename = "img.png";
        String savedFilename = UUID.randomUUID() + "_" + originalFilename;
        Path uploadPath = Paths.get(uploadDir);
        Path targetLocation = uploadPath.resolve(savedFilename);

        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("업로드 디렉토리 생성: {}", uploadPath);
            }
        } catch (IOException e) {
            log.error("업로드 디렉토리 생성 실패: {}", uploadPath, e);

        }

        Inquiry.FileInfo fileInfo = new Inquiry.FileInfo(
                originalFilename,
                savedFilename,
                targetLocation.toString()
        );
        List<Inquiry.FileInfo> attachedFiles = List.of(fileInfo);

        Inquiry testInquiry = new Inquiry(
                null,
                "배송이 너무 늦어요 (이미지 첨부)",
                "상품이 3일째 오지 않고 있습니다. 첨부 이미지 확인 부탁드립니다.",
                InquiryCategory.COMPLAINT,
                LocalDateTime.now().minusDays(1),
                customer,
                null,
                attachedFiles
        );
        inquiryRepository.save(testInquiry);
        log.info("테스트 데이터 주입 성공");
    }
}