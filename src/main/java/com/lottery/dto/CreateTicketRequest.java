package com.lottery.dto;

import java.util.List;

public record CreateTicketRequest(List<Integer> numbers) {
}
