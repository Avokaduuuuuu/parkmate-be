package com.parkmate.user;

import com.parkmate.common.exception.AppException;
import com.parkmate.common.exception.ErrorCode;
import com.parkmate.user.dto.CreateUserRequest;
import com.parkmate.user.dto.UserResponse;
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
