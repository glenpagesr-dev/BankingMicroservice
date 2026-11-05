package com.abc.banking;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * Banking Microservice: a minimal REST-style service for the DevOps capstone.
 */
public class App {

    static final String VERSION = "1.2";

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", exchange ->
                respond(exchange, "Banking Microservice v" + VERSION + " is running"));
        server.createContext("/health", exchange ->
                respond(exchange, "{\"status\":\"" + healthStatus() + "\"}"));
        server.createContext("/accounts/demo", exchange ->
                respond(exchange, accountSummary()));

        server.start();
        System.out.println("Banking Microservice started on port " + port);
    }

    static String healthStatus() {
        return "UP";
    }

    static String accountSummary() {
        return "{\"accountId\":\"ACC-1001\",\"owner\":\"Demo Customer\","
                + "\"balance\":\"2500.00\",\"currency\":\"USD\"}";
    }

    static BigDecimal deposit(BigDecimal balance, BigDecimal amount) {
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        return balance.add(amount);
    }

    static BigDecimal withdraw(BigDecimal balance, BigDecimal amount) {
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        if (amount.compareTo(balance) > 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }
        return balance.subtract(amount);
    }

    private static void respond(HttpExchange exchange, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }
}