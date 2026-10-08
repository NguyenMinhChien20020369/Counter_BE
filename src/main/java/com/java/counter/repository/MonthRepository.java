package com.java.counter.repository;

import com.java.counter.entity.DateCount;
import com.java.counter.entity.Month;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MonthRepository extends JpaRepository<Month, UUID> {
    Optional<Month> findByMonthAndYearAndUserId(Integer month, Integer year, UUID user_id);

    List<Month> findAllByYearAndMonth(Integer year, Integer month);
}
