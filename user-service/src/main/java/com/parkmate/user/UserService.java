package com.parkmate.user;

import com.parkmate.user.dto.ImportUserResponse;
import com.parkmate.user.dto.UpdateUserRequest;
import com.parkmate.user.dto.UserResponse;
import com.parkmate.user.dto.UserSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {

    UserResponse getUserById(Long id);

    List<UserResponse> getAllUsers();

    UserResponse getCurrentUser(String userIdHeader);

    Page<UserResponse> getAllUsers(int page, int size, String sortBy, String sortOrder, UserSearchCriteria criteria);

    UserResponse updateUser(Long id, UpdateUserRequest request);

    ImportUserResponse importUsersFromExcel(MultipartFile file);

    long count();

    void exportUsersToExcel(UserSearchCriteria criteria, java.io.OutputStream outputStream) throws java.io.IOException;

    void deleteUser(Long id);

}
