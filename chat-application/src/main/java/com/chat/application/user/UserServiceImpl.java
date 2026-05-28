package com.chat.application.user;

import com.chat.domain.common.IdGenerator;
import com.chat.domain.exception.CdlException;
import com.chat.domain.exception.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserCreateResponse createUser(String userId, String username) {
        if (userRepository.existsByUserId(userId)) {
            throw new CdlException(ExceptionCode.USER_ID_ALREADY_TAKEN);
        }
        UserEntity user = userRepository.save(UserEntity.create(userId, username));
        return UserCreateResponse.from(user);
    }
}
