package com.chat.application.user;

import com.chat.domain.common.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserEntity createUser(String username) {
        UserEntity user = UserEntity.create(username);
        return userRepository.save(user);
    }
}
