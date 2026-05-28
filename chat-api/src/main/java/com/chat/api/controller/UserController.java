package com.chat.api.controller;

import com.chat.api.dto.UserCreateRequest;
import com.chat.application.user.UserCreateResponse;
import com.chat.application.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public UserCreateResponse createUser(@RequestBody UserCreateRequest request) {
        return userService.createUser(request.userId(), request.username());
    }
}
