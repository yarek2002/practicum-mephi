package com.lottery.dto;

import com.lottery.model.DrawStatus;

import java.time.Instant;
import java.util.List;

public record DrawResponse(
        Long id,
        DrawStatus status,
        int numbersCount,
        int maxNumber,
        List<Integer> winningNumbers,
        Instant createdAt,
        Instant completedAt
) {
}
