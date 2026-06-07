package com.landmarket.controller;

import com.landmarket.dto.ApiResponse;
import com.landmarket.dto.ForgotPasswordRequest;
import com.landmarket.dto.ResetPasswordRequest;
import com.landmarket.model.User;
import com.landmarket.repository.UserRepository;
import com.landmarket.service.EmailService;
import java.time.LocalDateTime;
import java.util.UUID;
import com.landmarket.dto.AuthResponse;
import com.landmarket.dto.LoginRequest;
import com.landmarket.dto.RegisterRequest;
import com.landmarket.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired private AuthService authService;
    @Autowired private UserRepository userRepository;
    @Autowired private EmailService emailService;
    @Autowired private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.ok(ApiResponse.success("Registration successful", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(ApiResponse.success("Login successful", response));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid email or password"));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<?>> forgotPassword(
            @RequestBody ForgotPasswordRequest request) {
        try {
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("No account found with this email"));

            String token = UUID.randomUUID().toString();
            user.setResetToken(token);
            user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(30));
            userRepository.save(user);

            emailService.sendPasswordResetEmail(user.getEmail(), token);

            return ResponseEntity.ok(ApiResponse.success(
                "Password reset link sent to " + user.getEmail() + "! Check your inbox."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<?>> resetPassword(
            @RequestBody ResetPasswordRequest request) {
        try {
            User user = userRepository.findByResetToken(request.getToken())
                    .orElseThrow(() -> new RuntimeException("Invalid or expired reset link"));

            if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Reset link has expired! Please request a new one."));
            }

            if (request.getNewPassword().length() < 6) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Password must be at least 6 characters"));
            }

            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            user.setResetToken(null);
            user.setResetTokenExpiry(null);
            userRepository.save(user);

            return ResponseEntity.ok(ApiResponse.success("Password reset successfully! Please login."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
