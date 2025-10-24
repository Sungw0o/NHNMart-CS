package com.nhnacademy.nhnmartcs.user;

import com.nhnacademy.nhnmartcs.global.exception.LoginFailedException;
import com.nhnacademy.nhnmartcs.user.domain.Customer;
import com.nhnacademy.nhnmartcs.user.domain.User;
import com.nhnacademy.nhnmartcs.user.repository.UserRepository;
import com.nhnacademy.nhnmartcs.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new Customer();
        testUser.setUserId(1L);
        testUser.setLoginId("testUser");
        testUser.setPassword("password123");
        testUser.setName("테스트사용자");
    }

    @Test
    @DisplayName("로그인 성공")
    void doLogin_Success() {
        when(userRepository.findByLoginId("testUser")).thenReturn(Optional.of(testUser));
        User loggedInUser = userService.doLogin("testUser", "password123");

        assertThat(loggedInUser).isNotNull();
        assertThat(loggedInUser.getLoginId()).isEqualTo("testUser");
        assertThat(loggedInUser.getPassword()).isEqualTo("password123");
    }

    @Test
    @DisplayName("로그인 실패 - 아이디 없음")
    void doLogin_Fail_UserNotFound() {
        when(userRepository.findByLoginId(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.doLogin("wrongUser", "password123"))
                .isInstanceOf(LoginFailedException.class)
                .hasMessage("아이디가 존재하지 않습니다.");
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void doLogin_Fail_PasswordMismatch() {
        when(userRepository.findByLoginId("testUser")).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> userService.doLogin("testUser", "wrongPassword"))
                .isInstanceOf(LoginFailedException.class)
                .hasMessage("비밀번호가 일치하지 않습니다.");
    }
}
