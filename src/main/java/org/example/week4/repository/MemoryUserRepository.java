package org.example.week4.repository;

import org.example.week4.domain.User;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;


@Repository
public class MemoryUserRepository implements UserRepository {

    private final ConcurrentHashMap<Long, User> users = new ConcurrentHashMap<>();
    private final AtomicLong counter = new AtomicLong(1);

    @Override
    public void init(List<User> initialUsers) {
        users.clear();

        long maxId = 0;

        for (User user : initialUsers) {
            users.put(user.getId(), user);
            maxId = Math.max(maxId, user.getId());
        }

        counter.set(maxId + 1);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return users.values().stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst();
    }

    @Override
    public Optional<User> findByNickname(String nickname) {
        return users.values().stream()
                .filter(user -> user.getNickname().equals(nickname))
                .findFirst();
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public User save(User user) {
        Long id = user.getId();

        if (id == null) {
            id = counter.getAndIncrement();

            User savedUser = new User(
                    id,
                    user.getEmail(),
                    user.getPassword(),
                    user.getNickname(),
                    user.getFileId()
            );

            users.put(id, savedUser);
            return savedUser;
        }

        Long savedId = id;
        counter.updateAndGet(current -> Math.max(current, savedId + 1));
        users.put(id, user);
        return user;
    }

    @Override
    public void deleteById(Long id) {
        users.remove(id);
    }
}
