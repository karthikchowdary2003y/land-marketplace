package com.landmarket.service;

import com.landmarket.dto.AuthResponse;
import com.landmarket.dto.LoginRequest;
import com.landmarket.dto.RegisterRequest;
import com.landmarket.model.User;
import com.landmarket.repository.UserRepository;
import com.landmarket.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private JwtTokenProvider tokenProvider;
    @Autowired private JavaMailSender mailSender;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    // In-memory token store — no database table needed
    private final Map<String, TokenData> resetTokens = new ConcurrentHashMap<>();

    private static class TokenData {
        String email;
        LocalDateTime expiry;
        TokenData(String email) {
            this.email = email;
            this.expiry = LocalDateTime.now().plusHours(1);
        }
        boolean isExpired() {
            return LocalDateTime.now().isAfter(expiry);
        }
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Phone number already registered");
        }
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .active(true)
                .build();
        userRepository.save(user);
        String token = tokenProvider.generateTokenFromEmail(user.getEmail());
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword()));
        String token = tokenProvider.generateToken(authentication);
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .build();
    }

    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No account found with this email"));

        String token = UUID.randomUUID().toString();
        resetTokens.put(token, new TokenData(email));

        String resetLink = frontendUrl + "?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("onboarding@resend.dev");
        message.setTo(email);
        message.setSubject("LandMart - Reset Your Password");
        message.setText(
            "Hi " + user.getFullName() + ",\n\n" +
            "Click the link below to reset your password:\n\n" +
            resetLink + "\n\n" +
            "This link expires in 1 hour.\n\n" +
            "If you didn't request this, ignore this email.\n\n" +
            "— LandMart Team"
        );
        mailSender.send(message);
    }

    public void resetPassword(String token, String newPassword) {
        TokenData data = resetTokens.get(token);
        if (data == null || data.isExpired()) {
            throw new RuntimeException("Reset link is invalid or has expired");
        }

        User user = userRepository.findByEmail(data.email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetTokens.remove(token); // invalidate token after use
    }
}
