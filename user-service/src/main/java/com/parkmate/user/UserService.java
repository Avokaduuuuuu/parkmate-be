package com.parkmate.user;

import com.parkmate.user.dto.UserResponse;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface UserService {

    UserResponse getUserById(Long id);

    List<UserResponse> getAllUsers();

    UserResponse getCurrentUser(Authentication authentication, String userIdHeader);

}
