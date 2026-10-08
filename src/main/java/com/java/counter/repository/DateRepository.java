package com.java.counter.repository;

import com.java.counter.entity.DateCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DateRepository extends JpaRepository<DateCount, UUID> {
    Optional<DateCount> findByDateAndUserId(Date date, UUID user_id);
}
