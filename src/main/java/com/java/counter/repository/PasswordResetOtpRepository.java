package com.java.counter.repository;

import com.java.counter.entity.PasswordResetOtp;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PasswordResetOtp> findFirstByEmailOrderByCreatedAtDesc(String email);

    void deleteByEmail(String email);

    @Modifying
    @Query("""
            delete from PasswordResetOtp otp
            where otp.used = true or otp.expiresAt <= :now
            """)
    int deleteUsedOrExpired(@Param("now") Instant now);
}
