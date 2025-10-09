package com.parkmate.user;

import com.parkmate.common.dto.ApiResponse;
import com.parkmate.user.dto.ImportUserResponse;
import com.parkmate.user.dto.UpdateUserRequest;
import com.parkmate.user.dto.UserResponse;
import com.parkmate.user.dto.UserSearchCriteria;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/user-service/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Endpoints for user profile management")
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Get all users")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(value = "sortOrder", defaultValue = "asc") String sortOrder,
            @ModelAttribute UserSearchCriteria criteria
    ) {
        Page<UserResponse> users = userService.getAllUsers(page, size, sortBy, sortOrder, criteria);
        return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user, "User retrieved successfully"));
    }

    @GetMapping("/me")
    @Operation(
            summary = "Get current user profile",
            description = "Requires authentication. Click 'Authorize' button and enter JWT token.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @Parameter(hidden = true) Authentication authentication) {

        UserResponse user = userService.getCurrentUser(authentication, userIdHeader);
        return ResponseEntity.ok(ApiResponse.success(user, "User profile retrieved successfully"));
    }

    @PutMapping
    @Operation(
            summary = "Update user profile",
            description = "Requires authentication. Click 'Authorize' button and enter JWT token.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @Parameter(hidden = true) Authentication authentication,
            @RequestBody UpdateUserRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                userService.updateUser(
                        userService.getCurrentUser(authentication, userIdHeader).id(),
                        request),
                "User profile updated successfully"));
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Import users from Excel file")
    public ResponseEntity<ImportUserResponse> importUsers(
            @RequestParam("file") MultipartFile file
    ) {
        ImportUserResponse response = userService.importUsersFromExcel(file);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/count")
    @Operation(
            summary = "Count Users",
            description = "Count total number of users with optional filtering."
    )
    public ResponseEntity<ApiResponse<Long>> countUsers() {
        long count = userService.count();
        return ResponseEntity.ok(ApiResponse.success("Users counted successfully", count));
    }

    @GetMapping("/export")
    @Operation(
            summary = "Export Users to Excel",
            description = "Export all users (or filtered users) to an Excel file."
    )
    public void exportUsers(
            jakarta.servlet.http.HttpServletResponse response,
            @RequestBody(required = false) UserSearchCriteria criteria
    ) throws java.io.IOException {
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=users.xlsx");
        userService.exportUsersToExcel(criteria, response.getOutputStream());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user by ID")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    }

}
