package com.lottery.config;

public final class AppConfig {

    public static final int DEFAULT_PORT = 8080;
    public static final int DEFAULT_NUMBERS_COUNT = 6;
    public static final int DEFAULT_MAX_NUMBER = 49;

    private AppConfig() {
    }

    public static int getPort() {
        String port = System.getenv("PORT");
        if (port != null && !port.isBlank()) {
            return Integer.parseInt(port);
        }
        return DEFAULT_PORT;
    }
}
