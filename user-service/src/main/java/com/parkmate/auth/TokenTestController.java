package com.parkmate.auth;

import com.parkmate.common.dto.ApiResponse;
import com.parkmate.common.enums.AccountRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/test/tokens")
@RequiredArgsConstructor
@Tag(name = "Token Test", description = "Test endpoints to generate tokens for different roles (Development only)")
public class TokenTestController {

    private final JwtUtil jwtUtil;

    @GetMapping("/admin")
    @Operation(summary = "Generate ADMIN token", description = "Generate a test JWT token with ADMIN role")
    public ResponseEntity<ApiResponse<Map<String, String>>> generateAdminToken() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 1L);
        claims.put("email", "admin@parkmate.com");
        claims.put("username", "admin");
        claims.put("role", AccountRole.ADMIN.toString());

        String token = jwtUtil.generateToken(claims);

        Map<String, String> response = new HashMap<>();
        response.put("accessToken", token);
        response.put("role", "ADMIN");
        response.put("tokenType", "Bearer");

        return ResponseEntity.ok(ApiResponse.success(response, "Admin token generated"));
    }

    @GetMapping("/partner")
    @Operation(summary = "Generate PARTNER token", description = "Generate a test JWT token with PARTNER_OWNER role")
    public ResponseEntity<ApiResponse<Map<String, String>>> generatePartnerToken() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 2L);
        claims.put("email", "partner@parkmate.com");
        claims.put("username", "partner");
        claims.put("role", AccountRole.PARTNER_OWNER.toString());

        String token = jwtUtil.generateToken(claims);

        Map<String, String> response = new HashMap<>();
        response.put("accessToken", token);
        response.put("role", "PARTNER_OWNER");
        response.put("tokenType", "Bearer");

        return ResponseEntity.ok(ApiResponse.success(response, "Partner token generated"));
    }

    @GetMapping("/member")
    @Operation(summary = "Generate MEMBER token", description = "Generate a test JWT token with MEMBER role")
    public ResponseEntity<ApiResponse<Map<String, String>>> generateMemberToken() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 3L);
        claims.put("email", "member@parkmate.com");
        claims.put("username", "member");
        claims.put("role", AccountRole.MEMBER.toString());

        String token = jwtUtil.generateToken(claims);

        Map<String, String> response = new HashMap<>();
        response.put("accessToken", token);
        response.put("role", "MEMBER");
        response.put("tokenType", "Bearer");

        return ResponseEntity.ok(ApiResponse.success(response, "Member token generated"));
    }

    @GetMapping("/custom")
    @Operation(summary = "Generate custom token", description = "Generate a custom JWT token with specified parameters")
    public ResponseEntity<ApiResponse<Map<String, String>>> generateCustomToken(
            @RequestParam(defaultValue = "1") Long userId,
            @RequestParam(defaultValue = "test@parkmate.com") String email,
            @RequestParam(defaultValue = "testuser") String username,
            @RequestParam(defaultValue = "MEMBER") String role
    ) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        claims.put("username", username);
        claims.put("role", role);

        String token = jwtUtil.generateToken(claims);

        Map<String, String> response = new HashMap<>();
        response.put("accessToken", token);
        response.put("role", role);
        response.put("tokenType", "Bearer");
        response.put("userId", userId.toString());
        response.put("email", email);
        response.put("username", username);

        return ResponseEntity.ok(ApiResponse.success(response, "Custom token generated"));
    }
}
