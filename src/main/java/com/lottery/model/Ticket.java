package com.lottery.model;

import java.time.Instant;
import java.util.List;

public class Ticket {

    private Long id;
    private Long drawId;
    private List<Integer> numbers;
    private TicketStatus status;
    private Instant createdAt;

    public Ticket() {
    }

    public Ticket(Long id, Long drawId, List<Integer> numbers, TicketStatus status, Instant createdAt) {
        this.id = id;
        this.drawId = drawId;
        this.numbers = numbers;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDrawId() {
        return drawId;
    }

    public void setDrawId(Long drawId) {
        this.drawId = drawId;
    }

    public List<Integer> getNumbers() {
        return numbers;
    }

    public void setNumbers(List<Integer> numbers) {
        this.numbers = numbers;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
