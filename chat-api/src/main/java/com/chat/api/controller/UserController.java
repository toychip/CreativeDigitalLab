package com.chat.api.controller;

import com.chat.api.dto.UserCreateRequest;
import com.chat.api.dto.UserCreateResponse;
import com.chat.api.exception.ApiException;
import com.chat.application.user.UserEntity;
import com.chat.application.user.UserExceptionCode;
import com.chat.application.user.UserRepository;
import com.chat.domain.common.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @PostMapping
    public UserCreateResponse createUser(@RequestBody UserCreateRequest request) {
        UserEntity user = UserEntity.create(IdGenerator.generate(), request.username());
        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new ApiException(HttpStatus.CONFLICT, UserExceptionCode.USERNAME_ALREADY_TAKEN);
        }
        return new UserCreateResponse(user.getUserId(), user.getUsername());
    }
}
