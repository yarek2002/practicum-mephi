package com.lottery.service;

import com.lottery.dto.CreateTicketRequest;
import com.lottery.dto.TicketResponse;
import com.lottery.exception.BusinessException;
import com.lottery.exception.NotFoundException;
import com.lottery.model.Draw;
import com.lottery.model.Ticket;
import com.lottery.model.TicketStatus;
import com.lottery.repository.TicketRepository;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TicketService {

    private final TicketRepository ticketRepository;
    private final DrawService drawService;

    public TicketService(TicketRepository ticketRepository, DrawService drawService) {
        this.ticketRepository = ticketRepository;
        this.drawService = drawService;
    }

    public TicketResponse createTicket(Long drawId, CreateTicketRequest request) {
        Draw draw = drawService.getDrawOrThrow(drawId);

        if (!draw.isActive()) {
            throw new BusinessException("Cannot buy ticket for completed draw");
        }

        List<Integer> numbers = request.numbers();
        validateTicketNumbers(numbers, draw.getNumbersCount(), draw.getMaxNumber());

        Ticket ticket = new Ticket();
        ticket.setDrawId(drawId);
        ticket.setNumbers(numbers);
        ticket.setStatus(TicketStatus.PENDING);
        ticket.setCreatedAt(Instant.now());

        Ticket saved = ticketRepository.save(ticket);
        return toResponse(saved, draw.getWinningNumbers());
    }

    public TicketResponse getTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NotFoundException("Ticket not found: " + ticketId));

        Draw draw = drawService.getDrawOrThrow(ticket.getDrawId());
        return toResponse(ticket, draw.getWinningNumbers());
    }

    private void validateTicketNumbers(List<Integer> numbers, int expectedCount, int maxNumber) {
        if (numbers == null || numbers.isEmpty()) {
            throw new BusinessException("Ticket numbers are required");
        }
        if (numbers.size() != expectedCount) {
            throw new BusinessException("Ticket must contain exactly " + expectedCount + " numbers");
        }

        Set<Integer> unique = new HashSet<>();
        for (Integer number : numbers) {
            if (number == null || number < 1 || number > maxNumber) {
                throw new BusinessException("Each number must be between 1 and " + maxNumber);
            }
            if (!unique.add(number)) {
                throw new BusinessException("Ticket numbers must be unique");
            }
        }
    }

    private TicketResponse toResponse(Ticket ticket, List<Integer> winningNumbers) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getDrawId(),
                ticket.getNumbers(),
                ticket.getStatus(),
                winningNumbers,
                ticket.getCreatedAt()
        );
    }
}
