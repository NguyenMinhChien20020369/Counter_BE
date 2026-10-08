package com.java.counter.service;

import com.java.counter.dto.CountDTO;
import com.java.counter.dto.GetCountDTO;
import com.java.counter.dto.GetCountResponse;
import com.java.counter.entity.DateCount;
import com.java.counter.entity.Month;
import com.java.counter.entity.User;
import com.java.counter.repository.DateRepository;
import com.java.counter.repository.MonthRepository;
import com.java.counter.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class CountService {

    private final DateRepository dateRepository;
    private final UserRepository userRepository;
    private final MonthRepository monthRepository;

    public CountService(DateRepository dateRepository, UserRepository userRepository, MonthRepository monthRepository) {
        this.dateRepository = dateRepository;
        this.userRepository = userRepository;
        this.monthRepository = monthRepository;
    }

    public GetCountResponse get(GetCountDTO getCountDTO, UUID userId) {
        return monthRepository.findByMonthAndYearAndUserId(getCountDTO.month(), getCountDTO.year(), userId)
                .map(existingMonth -> {
                    return new GetCountResponse(existingMonth.getCount(), existingMonth.getDataCount());
                })
                .orElseGet(() -> {
                    return new GetCountResponse(0, null);
                });
    }

    public void change(CountDTO countDTO, UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user với ID: " + userId));

        Month month = monthRepository.findByMonthAndYearAndUserId(countDTO.date().getMonth() + 1, countDTO.date().getYear() + 1900, userId)
                .orElseGet(() -> {
                    Month newMonth = new Month();
                    newMonth.setMonth(countDTO.date().getMonth() + 1);
                    newMonth.setYear(countDTO.date().getYear() + 1900);
                    newMonth.setUser(user);

                    return monthRepository.save(newMonth);
                });

        Optional<DateCount> dateCount = dateRepository.findByDateAndUserId(countDTO.date(), userId);

        if (dateCount.isPresent()) {
            // === TRƯỜNG HỢP 1: TÌM THẤY BẢN GHI ===
            DateCount existingDateCount = dateCount.get();

            // Thực hiện cập nhật cộng dồn số đếm mới
            int newCount = existingDateCount.getCount() + countDTO.count();
            existingDateCount.setCount(Math.max(0, newCount)); // Đảm bảo số không bị âm

            dateRepository.save(existingDateCount);

            month.setCount(month.getCount() + countDTO.count());

            monthRepository.save(month);
        } else {
            // === TRƯỜNG HỢP 2: KHÔNG TÌM THẤY BẢN GHI ===

            DateCount newDateCount = new DateCount();
            newDateCount.setDate(countDTO.date());
            newDateCount.setUser(user);
            newDateCount.setCount(Math.max(0, countDTO.count())); // Nếu amount âm thì mặc định là 0
            newDateCount.setMonth(month);

            dateRepository.save(newDateCount);

            if (month.getCount() == null) {
                month.setCount(countDTO.count());
            } else {
                month.setCount(month.getCount() + countDTO.count());
            }
            monthRepository.save(month);
        }
    }
}
