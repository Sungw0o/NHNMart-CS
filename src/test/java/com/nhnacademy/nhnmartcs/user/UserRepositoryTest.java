package com.nhnacademy.nhnmartcs.user;

import com.nhnacademy.nhnmartcs.user.domain.Customer;
import com.nhnacademy.nhnmartcs.user.domain.User;
import com.nhnacademy.nhnmartcs.user.repository.impl.UserRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

class UserRepositoryTest {

    private UserRepositoryImpl userRepository;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        userRepository = new UserRepositoryImpl();


        Field storeField = UserRepositoryImpl.class.getDeclaredField("store");
        storeField.setAccessible(true);

        ((Map<Long, User>) storeField.get(null)).clear();

        Field sequenceField = UserRepositoryImpl.class.getDeclaredField("sequence");
        sequenceField.setAccessible(true);
        ((AtomicLong) sequenceField.get(null)).set(0L);
    }

    @Test
    @DisplayName("사용자 저장 및 ID 생성 확인")
    void save() {
        // Given
        Customer customer = new Customer();
        customer.setLoginId("testUser");
        customer.setPassword("password");
        customer.setName("테스트유저");

        User savedUser = userRepository.save(customer);

        assertThat(savedUser.getUserId()).isEqualTo(1L);
        assertThat(savedUser.getLoginId()).isEqualTo("testUser");
        assertThat(savedUser).isInstanceOf(Customer.class);
    }

    @Test
    @DisplayName("기존 사용자 정보 업데이트 확인 (save 메서드)")
    void save_updateExistingUser() {

        Customer customer = new Customer();
        customer.setLoginId("testUser");
        customer.setPassword("password");
        customer.setName("테스트유저");
        User initialSave = userRepository.save(customer);
        assertThat(initialSave.getUserId()).isEqualTo(1L);

        initialSave.setName("변경된이름");
        User updatedUser = userRepository.save(initialSave);

        assertThat(updatedUser.getUserId()).isEqualTo(1L);
        assertThat(updatedUser.getName()).isEqualTo("변경된이름");

        Optional<User> userInStore = userRepository.findById(1L);
        assertThat(userInStore).isPresent();
        assertThat(userInStore.get().getName()).isEqualTo("변경된이름");
    }


    @Test
    @DisplayName("ID로 사용자 찾기 - 성공")
    void findById_Found() {

        Customer customer = new Customer();
        customer.setLoginId("testUser");
        User savedUser = userRepository.save(customer);

        Optional<User> foundUserOpt = userRepository.findById(1L);

        assertThat(foundUserOpt).isPresent();
        assertThat(foundUserOpt.get()).isEqualTo(savedUser);
        assertThat(foundUserOpt.get().getUserId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("ID로 사용자 찾기 - 실패 (없는 ID)")
    void findById_NotFound() {

        Optional<User> foundUserOpt = userRepository.findById(999L);

        assertThat(foundUserOpt).isNotPresent();
    }

    @Test
    @DisplayName("로그인 ID로 사용자 찾기 - 성공")
    void findByLoginId_Found() {

        Customer customer1 = new Customer();
        customer1.setLoginId("user1");
        userRepository.save(customer1);

        Customer customer2 = new Customer();
        customer2.setLoginId("user2");
        userRepository.save(customer2);

        Optional<User> foundUserOpt = userRepository.findByLoginId("user1");


        assertThat(foundUserOpt).isPresent();
        assertThat(foundUserOpt.get().getLoginId()).isEqualTo("user1");
        assertThat(foundUserOpt.get()).isEqualTo(customer1);
    }

    @Test
    @DisplayName("로그인 ID로 사용자 찾기 - 실패 (없는 ID)")
    void findByLoginId_NotFound() {

        Customer customer = new Customer();
        customer.setLoginId("existingUser");
        userRepository.save(customer);

        Optional<User> foundUserOpt = userRepository.findByLoginId("nonExistingUser");

        assertThat(foundUserOpt).isNotPresent();
    }
}

