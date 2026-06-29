package com.lottery.service;

import com.lottery.config.AppConfig;
import com.lottery.dto.CreateDrawRequest;
import com.lottery.dto.DrawResponse;
import com.lottery.exception.BusinessException;
import com.lottery.exception.NotFoundException;
import com.lottery.model.Draw;
import com.lottery.model.DrawStatus;
import com.lottery.model.Ticket;
import com.lottery.model.TicketStatus;
import com.lottery.repository.DrawRepository;
import com.lottery.repository.TicketRepository;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DrawService {

    private final DrawRepository drawRepository;
    private final TicketRepository ticketRepository;
    private final SecureRandom random = new SecureRandom();

    public DrawService(DrawRepository drawRepository, TicketRepository ticketRepository) {
        this.drawRepository = drawRepository;
        this.ticketRepository = ticketRepository;
    }

    public DrawResponse createDraw(CreateDrawRequest request) {
        int numbersCount = request.numbersCount() != null ? request.numbersCount() : AppConfig.DEFAULT_NUMBERS_COUNT;
        int maxNumber = request.maxNumber() != null ? request.maxNumber() : AppConfig.DEFAULT_MAX_NUMBER;

        validateDrawParameters(numbersCount, maxNumber);

        Draw draw = new Draw();
        draw.setStatus(DrawStatus.ACTIVE);
        draw.setNumbersCount(numbersCount);
        draw.setMaxNumber(maxNumber);
        draw.setCreatedAt(Instant.now());

        Draw saved = drawRepository.save(draw);
        return toResponse(saved);
    }

    public List<DrawResponse> getActiveDraws() {
        return drawRepository.findByStatus(DrawStatus.ACTIVE).stream()
                .map(this::toResponse)
                .toList();
    }

    public DrawResponse completeDraw(Long drawId) {
        Draw draw = drawRepository.findById(drawId)
                .orElseThrow(() -> new NotFoundException("Draw not found: " + drawId));

        if (!draw.isActive()) {
            throw new BusinessException("Draw is already completed");
        }

        List<Integer> winningNumbers = generateWinningCombination(draw.getNumbersCount(), draw.getMaxNumber());
        draw.setWinningNumbers(winningNumbers);
        draw.setStatus(DrawStatus.COMPLETED);
        draw.setCompletedAt(Instant.now());
        drawRepository.update(draw);

        List<Ticket> tickets = ticketRepository.findByDrawId(drawId);
        for (Ticket ticket : tickets) {
            TicketStatus status = draw.combinationsMatch(ticket.getNumbers())
                    ? TicketStatus.WIN
                    : TicketStatus.LOSE;
            ticketRepository.updateStatus(ticket.getId(), status);
        }

        return toResponse(draw);
    }

    public Draw getDrawOrThrow(Long drawId) {
        return drawRepository.findById(drawId)
                .orElseThrow(() -> new NotFoundException("Draw not found: " + drawId));
    }

    private List<Integer> generateWinningCombination(int count, int maxNumber) {
        Set<Integer> numbers = new HashSet<>();
        while (numbers.size() < count) {
            numbers.add(random.nextInt(maxNumber) + 1);
        }
        List<Integer> result = new ArrayList<>(numbers);
        Collections.sort(result);
        return result;
    }

    private void validateDrawParameters(int numbersCount, int maxNumber) {
        if (numbersCount < 1) {
            throw new BusinessException("numbersCount must be at least 1");
        }
        if (maxNumber < numbersCount) {
            throw new BusinessException("maxNumber must be greater than or equal to numbersCount");
        }
    }

    private DrawResponse toResponse(Draw draw) {
        return new DrawResponse(
                draw.getId(),
                draw.getStatus(),
                draw.getNumbersCount(),
                draw.getMaxNumber(),
                draw.getWinningNumbers(),
                draw.getCreatedAt(),
                draw.getCompletedAt()
        );
    }
}
