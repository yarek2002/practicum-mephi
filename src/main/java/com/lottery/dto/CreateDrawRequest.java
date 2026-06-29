package com.lottery.dto;

import java.util.List;

public record CreateDrawRequest(Integer numbersCount, Integer maxNumber) {
}
