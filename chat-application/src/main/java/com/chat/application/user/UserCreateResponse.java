package com.chat.application.user;

public record UserCreateResponse(
        String userId,
        String username
) {
    public static UserCreateResponse from(UserEntity user) {
        return new UserCreateResponse(user.getUserId(), user.getUsername());
    }
}
