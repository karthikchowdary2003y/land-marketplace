package com.landmarket.dto;

import com.landmarket.model.User;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank
    @Email(message = "Valid email is required")
    private String email;

    @NotBlank
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Valid 10-digit phone number required")
    private String phone;

    @NotBlank
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private String address;
    private String city;
    private String state;

    @NotNull(message = "Role is required (SELLER or BUYER)")
    private User.Role role;
}
