package com.java.counter.service;

import com.java.counter.entity.Month;
import com.java.counter.repository.MonthRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;

@Service
public class CountDataCleanupService {
    private static final Logger logger = LoggerFactory.getLogger(CountDataCleanupService.class);
    private static final ZoneId SCHEDULE_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final MonthRepository monthRepository;

    public CountDataCleanupService(MonthRepository monthRepository) {
        this.monthRepository = monthRepository;
    }

    @Scheduled(cron = "0 0 2 1 * *", zone = "Asia/Ho_Chi_Minh")
    @Transactional
    public void deleteCountDataFromTwoMonthsAgo() {
        YearMonth targetMonth = YearMonth.now(SCHEDULE_ZONE).minusMonths(2);
        List<Month> oldMonths = monthRepository.findAllByYearAndMonth(
                targetMonth.getYear(),
                targetMonth.getMonthValue()
        );

        monthRepository.deleteAll(oldMonths);
        logger.info(
                "Deleted count data for {} from {} monthly records",
                targetMonth,
                oldMonths.size()
        );
    }
}
