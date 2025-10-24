package com.nhnacademy.nhnmartcs.user.service.impl;

import com.nhnacademy.nhnmartcs.global.exception.LoginFailedException;
import com.nhnacademy.nhnmartcs.user.domain.User;
import com.nhnacademy.nhnmartcs.user.repository.UserRepository;
import com.nhnacademy.nhnmartcs.user.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    /**
     * Authenticate a user by login ID and password.
     *
     * @param loginId the user's login identifier
     * @param password the plaintext password to verify
     * @return the authenticated User
     * @throws LoginFailedException if no user exists for the given loginId or if the password does not match
     */
    @Override
    public User doLogin(String loginId, String password){

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(()-> new LoginFailedException("아이디가 존재하지 않습니다."));

        if (!user.getPassword().equals(password)) {
            throw new LoginFailedException("비밀번호가 일치하지 않습니다.");
        }
        return user;
    }

}