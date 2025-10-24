package com.nhnacademy.nhnmartcs.user.repository;

import com.nhnacademy.nhnmartcs.user.domain.User;

import java.util.Optional;

public interface UserRepository {

    /**
 * Persists the given user entity and returns the persisted instance.
 *
 * @param user the user entity to persist
 * @return the persisted {@code User}, potentially with updated state (for example, a generated identifier)
 */
User save(User user);
    /**
 * Retrieve a user by their identifier.
 *
 * @param UserId the identifier of the user to retrieve
 * @return an Optional containing the User if found, otherwise an empty Optional
 */
Optional<User> findById(Long UserId);
    /**
 * Finds a user by their login identifier.
 *
 * @param loginId the login identifier of the user to find
 * @return an Optional containing the User if found, otherwise an empty Optional
 */
Optional<User> findByLoginId(String loginId);

}