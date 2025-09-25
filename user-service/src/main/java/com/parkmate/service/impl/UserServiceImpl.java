package com.parkmate.service.impl;

import com.parkmate.dto.request.CreateUserRequest;
import com.parkmate.dto.response.UserResponse;
import com.parkmate.entity.User;
import com.parkmate.exception.AppException;
import com.parkmate.exception.ErrorCode;
import com.parkmate.mapper.UserMapper;
import com.parkmate.repository.UserRepository;
import com.parkmate.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public UserResponse login(String phone, String password) {
        return null;
    }

    @Override
    public UserResponse register(CreateUserRequest request) {

        if (userRepository.existsByPhone(request.phone())) {
            throw new AppException(ErrorCode.PHONE_ALREADY_EXISTS, request.phone());
        }

        User user = userMapper.toEntity(request);

        //password

        return userMapper.toResponse(userRepository.save(user));
    }

    // Todo
    // - login
    // - update user details
    // - change password
    // - delete user
}
