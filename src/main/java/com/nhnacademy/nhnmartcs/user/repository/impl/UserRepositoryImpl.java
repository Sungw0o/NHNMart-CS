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

    @Override
    public User save(User user) {
        if (user.getUserId() == null) {
            user.setUserId(sequence.incrementAndGet());
        }

        store.put(user.getUserId(), user);
        return user;
    }

    @Override
    public Optional<User> findById(Long userId) {
        return Optional.ofNullable(store.get(userId));
    }

    @Override
    public Optional<User> findByLoginId(String loginId) {
        return store.values().stream()
                .filter(user -> user.getLoginId().equals(loginId))
                .findFirst();
    }
}
