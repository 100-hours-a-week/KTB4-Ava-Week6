package org.example.week4.repository;

import org.example.week4.domain.User;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository {
    void init(List<User> users);

    Optional<User> findByEmail(String email);

    Optional<User> findByNickname(String nickname);

    Optional<User> findById(Long id);

    User save(User user);

    void deleteById(Long id);
}
