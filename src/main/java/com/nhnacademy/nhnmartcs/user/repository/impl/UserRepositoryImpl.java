package com.nhnacademy.nhnmartcs.user.repository.impl;

import com.nhnacademy.nhnmartcs.user.domain.User;
import com.nhnacademy.nhnmartcs.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Repository
public class UserRepositoryImpl implements UserRepository {

    private static final Map<Long, User> store = new HashMap<>();
    private static final AtomicLong sequence = new AtomicLong(0L);

    /**
     * Persists the given user in the in-memory store, assigning a new ID when the user's ID is null.
     *
     * @param user the user to persist; if {@code user.getUserId()} is null a new ID will be assigned
     * @return the stored user, with a userId assigned if it was previously null
     */
    @Override
    public User save(User user) {
        if (user.getUserId() == null) {
            user.setUserId(sequence.incrementAndGet());
        }

        store.put(user.getUserId(), user);
        return user;
    }

    /**
     * Fetches a user by its identifier.
     *
     * @param userId the identifier of the user to retrieve
     * @return an Optional containing the matching User if found, empty otherwise
     */
    @Override
    public Optional<User> findById(Long userId) {
        return Optional.ofNullable(store.get(userId));
    }

    /**
     * Finds a user with the given login identifier.
     *
     * @param loginId the login identifier to search for
     * @return an Optional containing the first User with a matching loginId, or empty if none is found
     */
    @Override
    public Optional<User> findByLoginId(String loginId) {
        return store.values().stream()
                .filter(user -> user.getLoginId().equals(loginId))
                .findFirst();
    }
}