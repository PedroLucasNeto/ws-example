package com.messenger.chat.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.messenger.chat.model.User;
import com.messenger.chat.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User registerOrUpdateOnline(String name) {
        User user = userRepository.findByName(name).orElseGet(() -> User.builder()
                .name(name).online(true).lastSeen(LocalDateTime.now()).build());
        user.setOnline(true);
        user.setLastSeen(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Transactional
    public void setOffline(String name) {
        userRepository.findByName(name).ifPresent(u -> {
            u.setOnline(false);
            u.setLastSeen(LocalDateTime.now());
            userRepository.save(u);
        });
    }

    public List<User> listOnline() {
        return userRepository.findByOnlineTrue();
    }

    public List<User> listAll() {
        return userRepository.findAll();
    }
}
