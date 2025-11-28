package com.messenger.chat.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.messenger.chat.model.User;
import com.messenger.chat.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserRestController {
    private final UserService userService;

    public UserRestController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestParam String name) {
        User u = userService.registerOrUpdateOnline(name);
        return ResponseEntity.ok(u);
    }

    @GetMapping("/online")
    public ResponseEntity<List<User>> online() {
        return ResponseEntity.ok(userService.listOnline());
    }

    @GetMapping
    public ResponseEntity<List<User>> all() {
        return ResponseEntity.ok(userService.listAll());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestParam String name) {
        userService.setOffline(name);
        return ResponseEntity.ok().build();
    }
}