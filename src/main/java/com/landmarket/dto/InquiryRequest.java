package com.landmarket.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InquiryRequest {

    // For non-registered users
    private String inquirerName;
    private String inquirerPhone;
    private String inquirerEmail;

    @NotBlank(message = "Message is required")
    private String message;
}
