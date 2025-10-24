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
