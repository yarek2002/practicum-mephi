package com.lottery.dto;

import com.lottery.model.TicketStatus;

import java.time.Instant;
import java.util.List;

public record TicketResponse(
        Long id,
        Long drawId,
        List<Integer> numbers,
        TicketStatus status,
        List<Integer> winningNumbers,
        Instant createdAt
) {
}
