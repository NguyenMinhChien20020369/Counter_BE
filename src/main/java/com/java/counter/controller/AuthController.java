package com.java.counter.controller;

import com.java.counter.dto.AuthResponse;
import com.java.counter.dto.ForgotPasswordRequest;
import com.java.counter.dto.LoginRequest;
import com.java.counter.dto.ResetPasswordRequest;
import com.java.counter.dto.SignupRequest;
import com.java.counter.service.AuthService;
import com.java.counter.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    public AuthController(AuthService authService, PasswordResetService passwordResetService) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.ok(Map.of(
                "status", "success"
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        passwordResetService.requestOtp(request.email());
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Nếu email tồn tại, mã OTP sẽ được gửi đến hộp thư của bạn."
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        if (!passwordResetService.resetPassword(request)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "OTP không hợp lệ, đã hết hạn hoặc đã vượt quá số lần thử."
            ));
        }

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Đổi mật khẩu thành công. Vui lòng đăng nhập lại."
        ));
    }
}