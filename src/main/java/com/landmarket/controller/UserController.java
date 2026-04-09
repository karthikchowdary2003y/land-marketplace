package com.landmarket.controller;

import com.landmarket.dto.ApiResponse;
import com.landmarket.dto.ChangePasswordRequest;
import com.landmarket.model.User;
import com.landmarket.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * GET /api/users/me
     * Get current logged-in user profile
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<User>> getMyProfile(Authentication authentication) {
        User user = userService.getUserByEmail(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Profile fetched", user));
    }

    /**
     * GET /api/users/{id}
     * Get public profile of any user (to see seller details)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id);
            return ResponseEntity.ok(ApiResponse.success("User fetched", user));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * PUT /api/users/me
     * Update own profile
     */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<User>> updateProfile(
            @RequestBody Map<String, String> updates,
            Authentication authentication) {
        try {
            User updated = userService.updateProfile(
                    authentication.getName(),
                    updates.get("fullName"),
                    updates.get("address"),
                    updates.get("city"),
                    updates.get("state"),
                    updates.get("phone")
            );
            return ResponseEntity.ok(ApiResponse.success("Profile updated", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * POST /api/users/change-password
     * Change password
     */
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestBody ChangePasswordRequest body,
            Authentication authentication) {
        try {
            userService.changePassword(
                    authentication.getName(),
                    body.getOldPassword(),
                    body.getNewPassword()
            );
            return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
