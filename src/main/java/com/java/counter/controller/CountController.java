package com.java.counter.controller;

import java.util.Map;
import java.util.UUID;

import com.java.counter.dto.CountDTO;
import com.java.counter.dto.GetCountDTO;
import com.java.counter.dto.GetCountResponse;
import com.java.counter.service.CountService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/count")
public class CountController {
    private final CountService countService;

    public CountController(CountService countService) {
        this.countService = countService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> change(@RequestBody CountDTO countDTO, @AuthenticationPrincipal UUID userId) {
//        CurrentUser currentUser = currentUserProvider.get();
        countService.change(countDTO, userId);
        return ResponseEntity.ok(Map.of(
                "status", "success"
        ));
    }

    @GetMapping
    public ResponseEntity<GetCountResponse> get(@ModelAttribute GetCountDTO getCountDTO, @AuthenticationPrincipal UUID userId) {
//        CurrentUser currentUser = currentUserProvider.get();
//        countService.get(getCountDTO, userId);
        return ResponseEntity.ok(countService.get(getCountDTO, userId));
    }
}
