package com.parkmate.user;

import com.parkmate.user.dto.UpdateUserRequest;
import com.parkmate.user.dto.UserResponse;
import com.parkmate.user.dto.UserSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface UserService {

    UserResponse getUserById(Long id);

    List<UserResponse> getAllUsers();

    UserResponse getCurrentUser(Authentication authentication, String userIdHeader);

    Page<UserResponse> getAllUsers(int page, int size, String sortBy, String sortOrder, UserSearchCriteria criteria);

    UserResponse updateUser(Long id, UpdateUserRequest request);

}
