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

        // Return token directly — frontend shows reset form immediately
        return ResponseEntity.ok(ApiResponse.success(
            "Account verified! You can now reset your password.", token));
    } catch (RuntimeException e) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
    }
}
