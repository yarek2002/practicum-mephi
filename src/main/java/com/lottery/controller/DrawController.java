package com.lottery.controller;

import com.lottery.dto.CreateDrawRequest;
import com.lottery.dto.CreateTicketRequest;
import com.lottery.dto.DrawResponse;
import com.lottery.dto.ErrorResponse;
import com.lottery.dto.TicketResponse;
import com.lottery.exception.BusinessException;
import com.lottery.exception.NotFoundException;
import com.lottery.service.DrawService;
import com.lottery.service.TicketService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.util.List;

public class DrawController {

    private final DrawService drawService;
    private final TicketService ticketService;

    public DrawController(DrawService drawService, TicketService ticketService) {
        this.drawService = drawService;
        this.ticketService = ticketService;
    }

    public void registerRoutes(Javalin app) {
        app.post("/draws", this::createDraw);
        app.get("/draws", this::getActiveDraws);
        app.post("/draws/{id}/tickets", this::createTicket);
        app.post("/draws/{id}/complete", this::completeDraw);
        app.get("/tickets/{id}", this::getTicket);

        app.exception(NotFoundException.class, (e, ctx) -> {
            ctx.status(HttpStatus.NOT_FOUND);
            ctx.json(new ErrorResponse(e.getMessage()));
        });
        app.exception(BusinessException.class, (e, ctx) -> {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.json(new ErrorResponse(e.getMessage()));
        });
        app.exception(Exception.class, (e, ctx) -> {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.json(new ErrorResponse("Internal server error"));
        });
    }

    private void createDraw(Context ctx) {
        CreateDrawRequest request = ctx.bodyAsClass(CreateDrawRequest.class);
        DrawResponse response = drawService.createDraw(request);
        ctx.status(HttpStatus.CREATED);
        ctx.json(response);
    }

    private void getActiveDraws(Context ctx) {
        List<DrawResponse> draws = drawService.getActiveDraws();
        ctx.json(draws);
    }

    private void createTicket(Context ctx) {
        Long drawId = Long.parseLong(ctx.pathParam("id"));
        CreateTicketRequest request = ctx.bodyAsClass(CreateTicketRequest.class);
        TicketResponse response = ticketService.createTicket(drawId, request);
        ctx.status(HttpStatus.CREATED);
        ctx.json(response);
    }

    private void completeDraw(Context ctx) {
        Long drawId = Long.parseLong(ctx.pathParam("id"));
        DrawResponse response = drawService.completeDraw(drawId);
        ctx.json(response);
    }

    private void getTicket(Context ctx) {
        Long ticketId = Long.parseLong(ctx.pathParam("id"));
        TicketResponse response = ticketService.getTicket(ticketId);
        ctx.json(response);
    }
}
