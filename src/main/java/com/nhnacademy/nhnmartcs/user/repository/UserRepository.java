package com.nhnacademy.nhnmartcs.user.repository;

import com.nhnacademy.nhnmartcs.user.domain.User;

import java.util.Optional;

public interface UserRepository {

    User save(User user);
    Optional<User> findById(Long UserId);
    Optional<User> findByLoginId(String loginId);

}
