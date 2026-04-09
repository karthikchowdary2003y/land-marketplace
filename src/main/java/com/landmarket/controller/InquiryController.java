package com.landmarket.controller;

import com.landmarket.dto.ApiResponse;
import com.landmarket.dto.InquiryRequest;
import com.landmarket.model.Inquiry;
import com.landmarket.service.InquiryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inquiries")
@CrossOrigin(origins = "*")
public class InquiryController {

    @Autowired
    private InquiryService inquiryService;

    /**
     * POST /api/inquiries/land/{landId}
     * Send inquiry to land owner (can be guest or logged-in user)
     * Buyer message goes to seller — no middleman
     */
    @PostMapping("/land/{landId}")
    public ResponseEntity<ApiResponse<Inquiry>> sendInquiry(
            @PathVariable Long landId,
            @Valid @RequestBody InquiryRequest request,
            Authentication authentication) {
        try {
            String email = authentication != null ? authentication.getName() : null;
            Inquiry inquiry = inquiryService.sendInquiry(landId, request, email);
            return ResponseEntity.ok(ApiResponse.success(
                    "Inquiry sent! The owner will contact you directly.", inquiry));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * GET /api/inquiries/received
     * Get all inquiries received (Seller view)
     */
    @GetMapping("/received")
    public ResponseEntity<ApiResponse<List<Inquiry>>> getReceivedInquiries(
            Authentication authentication) {
        List<Inquiry> inquiries = inquiryService
                .getMyReceivedInquiries(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Received inquiries", inquiries));
    }

    /**
     * GET /api/inquiries/sent
     * Get all inquiries sent (Buyer view)
     */
    @GetMapping("/sent")
    public ResponseEntity<ApiResponse<List<Inquiry>>> getSentInquiries(
            Authentication authentication) {
        List<Inquiry> inquiries = inquiryService
                .getMySentInquiries(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Sent inquiries", inquiries));
    }

    /**
     * GET /api/inquiries/land/{landId}
     * Get all inquiries for a specific land (Seller only)
     */
    @GetMapping("/land/{landId}")
    public ResponseEntity<ApiResponse<List<Inquiry>>> getLandInquiries(
            @PathVariable Long landId,
            Authentication authentication) {
        try {
            List<Inquiry> inquiries = inquiryService
                    .getInquiriesForLand(landId, authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("Land inquiries", inquiries));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
