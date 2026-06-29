package com.lottery;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lottery.config.AppConfig;
import com.lottery.config.DatabaseConfig;
import com.lottery.controller.DrawController;
import com.lottery.repository.DrawRepository;
import com.lottery.repository.TicketRepository;
import com.lottery.service.DrawService;
import com.lottery.service.TicketService;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;

import javax.sql.DataSource;

public class Main {

    public static void main(String[] args) {
        DataSource dataSource = DatabaseConfig.getDataSource();

        DrawRepository drawRepository = new DrawRepository(dataSource);
        TicketRepository ticketRepository = new TicketRepository(dataSource);

        DrawService drawService = new DrawService(drawRepository, ticketRepository);
        TicketService ticketService = new TicketService(ticketRepository, drawService);

        DrawController drawController = new DrawController(drawService, ticketService);

        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Javalin app = Javalin.create(config -> {
            config.jsonMapper(new JavalinJackson(objectMapper, false));
            config.showJavalinBanner = false;
        });

        drawController.registerRoutes(app);

        app.get("/health", ctx -> ctx.result("OK"));

        int port = AppConfig.getPort();
        app.start(port);
        System.out.println("Lottery backend started on port " + port);
    }
}
