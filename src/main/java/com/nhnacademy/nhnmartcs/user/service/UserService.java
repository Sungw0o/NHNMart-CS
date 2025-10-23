package com.nhnacademy.nhnmartcs.user.service;

import com.nhnacademy.nhnmartcs.user.domain.User;

public interface UserService {

    User doLogin(String loginid, String password);
}
