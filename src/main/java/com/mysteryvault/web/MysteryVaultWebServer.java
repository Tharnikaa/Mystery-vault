package com.mysteryvault.web;

import com.mysteryvault.service.CaseService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * Embedded read-only diagnostics HTTP web server using JDK's built-in HttpServer.
 * Owned by Member 3 (Module 12: Diagnostics / JDBC Stub).
 * Disabled by default. Zero external servlet or Spring dependencies.
 */
public class MysteryVaultWebServer {

    private final int port;
    private final CaseService caseService;
    private HttpServer server;
    private boolean running = false;

    public MysteryVaultWebServer(int port, CaseService caseService) {
        this.port = port;
        this.caseService = caseService;
    }

    public synchronized void start() throws IOException {
        if (running) {
            return;
        }
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/diagnostics", new DiagnosticsHandler());
        server.createContext("/api/cases", new CasesDiagnosticsHandler());
        server.setExecutor(null); // default executor
        server.start();
        running = true;
    }

    public synchronized void stop() {
        if (running && server != null) {
            server.stop(0);
            running = false;
        }
    }

    public boolean isRunning() {
        return running;
    }

    public int getPort() {
        return port;
    }

    private class DiagnosticsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            Runtime runtime = Runtime.getRuntime();
            long totalMem = runtime.totalMemory() / (1024 * 1024);
            long freeMem = runtime.freeMemory() / (1024 * 1024);
            long usedMem = totalMem - freeMem;

            String json = String.format("{\n" +
                    "  \"status\": \"HEALTHY\",\n" +
                    "  \"timestamp\": \"%s\",\n" +
                    "  \"system\": {\n" +
                    "    \"availableProcessors\": %d,\n" +
                    "    \"usedMemoryMb\": %d,\n" +
                    "    \"totalMemoryMb\": %d\n" +
                    "  }\n" +
                    "}\n", LocalDateTime.now(), runtime.availableProcessors(), usedMem, totalMem);

            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    private class CasesDiagnosticsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            int caseCount = caseService != null ? caseService.listCases().size() : 0;
            String json = String.format("{\n" +
                    "  \"totalRegisteredCases\": %d,\n" +
                    "  \"readOnly\": true\n" +
                    "}\n", caseCount);

            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }
}
