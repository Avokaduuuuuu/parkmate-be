package com.parkmate.user;

import com.parkmate.account.AccountRepository;
import com.parkmate.common.exception.AppException;
import com.parkmate.common.exception.ErrorCode;
import com.parkmate.s3.S3Service;
import com.parkmate.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.parkmate.auth.AuthServiceImpl.getUserResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final S3Service s3Service;
    private final AccountRepository accountRepository;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return responseWithPresignedURL(userMapper.toResponse(user), user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> responseWithPresignedURL(userMapper.toResponse(user), user))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(Authentication authentication, String userIdHeader) {
        long accountId;

        // Try to get userId from header first (from gateway)
        if (userIdHeader != null && !userIdHeader.isEmpty()) {
            accountId = Long.parseLong(userIdHeader);
        } else if (authentication != null) {
            accountId = Long.parseLong(authentication.getName());
        } else {
            throw new RuntimeException("User not authenticated");
        }

        log.info("Fetching profile for user ID: {}", accountId);
        ;
        return getUserById(accountRepository.findById(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND))
                .getUser()
                .getId());
    }

    private UserResponse responseWithPresignedURL(UserResponse response, User user) {
        return getUserResponse(response, user, s3Service);
    }

    // Todo
    // - login
    // - change password
    // - delete user
}
