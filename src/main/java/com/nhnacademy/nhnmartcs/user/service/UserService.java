package com.nhnacademy.nhnmartcs.user.service;

import com.nhnacademy.nhnmartcs.user.domain.User;

public interface UserService {

    /**
 * Authenticate a user with the given login identifier and password.
 *
 * @param loginid the user's login identifier (e.g., username or email)
 * @param password the user's plain-text password to verify
 * @return the authenticated {@code User} if credentials are valid, otherwise implementation-defined (e.g., null or an exception)
 */
User doLogin(String loginid, String password);
}