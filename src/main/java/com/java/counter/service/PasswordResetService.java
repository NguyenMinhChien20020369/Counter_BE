package com.java.counter.service;

import com.java.counter.dto.ResetPasswordRequest;
import com.java.counter.entity.PasswordResetOtp;
import com.java.counter.entity.User;
import com.java.counter.repository.PasswordResetOtpRepository;
import com.java.counter.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;

@Service
public class PasswordResetService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Duration OTP_LIFETIME = Duration.ofMinutes(5);
    private static final Duration RESEND_COOLDOWN = Duration.ofSeconds(60);
    private static final int MAX_ATTEMPTS = 5;
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final PasswordResetOtpRepository otpRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final byte[] otpHmacSecret;

    public PasswordResetService(
            PasswordResetOtpRepository otpRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            @Value("${app.password-reset.otp-hmac-secret}") String otpHmacSecret
    ) {
        if (otpHmacSecret.length() < 32) {
            throw new IllegalArgumentException("OTP_HMAC_SECRET phải dài ít nhất 32 ký tự.");
        }
        this.otpRepository = otpRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.otpHmacSecret = otpHmacSecret.getBytes(StandardCharsets.UTF_8);
    }

    @Transactional
    public void requestOtp(String emailAddress) {
        String email = normalizeEmail(emailAddress);
        Instant now = Instant.now();

        var latestOtp = otpRepository.findFirstByEmailOrderByCreatedAtDesc(email);
        if (latestOtp.isPresent()
                && latestOtp.get().getCreatedAt().plus(RESEND_COOLDOWN).isAfter(now)) {
            return;
        }

        var user = userRepository.findByEmailIgnoreCase(email);
        if (user.isEmpty()) {
            return;
        }

        String otp = String.format(Locale.ROOT, "%06d", SECURE_RANDOM.nextInt(1_000_000));
        otpRepository.deleteByEmail(email);

        PasswordResetOtp resetOtp = new PasswordResetOtp();
        resetOtp.setEmail(email);
        resetOtp.setOtpHash(hashOtp(otp));
        resetOtp.setCreatedAt(now);
        resetOtp.setExpiresAt(now.plus(OTP_LIFETIME));
        resetOtp.setAttempts(0);
        resetOtp.setUsed(false);
        otpRepository.save(resetOtp);

        emailService.sendPasswordResetOtp(email, otp);
    }

    @Transactional
    public boolean resetPassword(ResetPasswordRequest request) {
        String email = normalizeEmail(request.email());
        Instant now = Instant.now();
        var otpResult = otpRepository.findFirstByEmailOrderByCreatedAtDesc(email);

        if (otpResult.isEmpty()) {
            return false;
        }

        PasswordResetOtp resetOtp = otpResult.get();
        if (resetOtp.isUsed()
                || !resetOtp.getExpiresAt().isAfter(now)
                || resetOtp.getAttempts() >= MAX_ATTEMPTS) {
            return false;
        }

        if (!MessageDigest.isEqual(
                resetOtp.getOtpHash().getBytes(StandardCharsets.US_ASCII),
                hashOtp(request.otp()).getBytes(StandardCharsets.US_ASCII))) {
            resetOtp.setAttempts(resetOtp.getAttempts() + 1);
            otpRepository.save(resetOtp);
            return false;
        }

        User user = userRepository.findByEmailIgnoreCase(email).orElse(null);
        if (user == null) {
            return false;
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        resetOtp.setUsed(true);
        userRepository.save(user);
        otpRepository.save(resetOtp);
        return true;
    }

    private String hashOtp(String otp) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(otpHmacSecret, HMAC_ALGORITHM));
            return HexFormat.of().formatHex(mac.doFinal(otp.getBytes(StandardCharsets.US_ASCII)));
        } catch (Exception exception) {
            throw new IllegalStateException("Không thể bảo vệ OTP.", exception);
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
