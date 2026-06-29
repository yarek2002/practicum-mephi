package com.lottery.model;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public class Draw {

    private Long id;
    private DrawStatus status;
    private int numbersCount;
    private int maxNumber;
    private List<Integer> winningNumbers;
    private Instant createdAt;
    private Instant completedAt;

    public Draw() {
    }

    public Draw(Long id, DrawStatus status, int numbersCount, int maxNumber,
                List<Integer> winningNumbers, Instant createdAt, Instant completedAt) {
        this.id = id;
        this.status = status;
        this.numbersCount = numbersCount;
        this.maxNumber = maxNumber;
        this.winningNumbers = winningNumbers;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DrawStatus getStatus() {
        return status;
    }

    public void setStatus(DrawStatus status) {
        this.status = status;
    }

    public int getNumbersCount() {
        return numbersCount;
    }

    public void setNumbersCount(int numbersCount) {
        this.numbersCount = numbersCount;
    }

    public int getMaxNumber() {
        return maxNumber;
    }

    public void setMaxNumber(int maxNumber) {
        this.maxNumber = maxNumber;
    }

    public List<Integer> getWinningNumbers() {
        return winningNumbers;
    }

    public void setWinningNumbers(List<Integer> winningNumbers) {
        this.winningNumbers = winningNumbers;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public boolean isActive() {
        return status == DrawStatus.ACTIVE;
    }

    public boolean combinationsMatch(List<Integer> ticketNumbers) {
        if (winningNumbers == null || ticketNumbers == null) {
            return false;
        }
        if (winningNumbers.size() != ticketNumbers.size()) {
            return false;
        }
        int[] winning = winningNumbers.stream().mapToInt(Integer::intValue).sorted().toArray();
        int[] ticket = ticketNumbers.stream().mapToInt(Integer::intValue).sorted().toArray();
        return Arrays.equals(winning, ticket);
    }
}
