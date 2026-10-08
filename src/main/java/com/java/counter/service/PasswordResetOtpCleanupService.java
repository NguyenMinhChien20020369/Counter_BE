package com.java.counter.service;

import com.java.counter.repository.PasswordResetOtpRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class PasswordResetOtpCleanupService {
    private static final Logger logger = LoggerFactory.getLogger(PasswordResetOtpCleanupService.class);

    private final PasswordResetOtpRepository otpRepository;

    public PasswordResetOtpCleanupService(PasswordResetOtpRepository otpRepository) {
        this.otpRepository = otpRepository;
    }

    @Scheduled(cron = "0 0 2 * * *", zone = "Asia/Ho_Chi_Minh")
    @Transactional
    public void deleteUsedOrExpiredOtps() {
        int deletedCount = otpRepository.deleteUsedOrExpired(Instant.now());
        logger.info("Deleted {} used or expired password reset OTP records", deletedCount);
    }
}
