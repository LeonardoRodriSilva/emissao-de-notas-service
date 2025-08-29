package com.handler;

import com.dto.CriarNotaFiscalRequest;
import com.entity.NotaFiscal;
import com.exception.PersistenciaException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.service.NotaFiscalService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
public class NotaFiscalHttpHandler implements HttpHandler {

    private final NotaFiscalService notaFiscalService;
    private final ObjectMapper objectMapper;

    public NotaFiscalHttpHandler(NotaFiscalService notaFiscalService, ObjectMapper objectMapper) {
        this.notaFiscalService = notaFiscalService;
        this.objectMapper = objectMapper;
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod().toUpperCase();
        String path = exchange.getRequestURI().getPath();

        if ("POST".equals(method) && "/api/notas-fiscais".equals(path)) {
            handlePostNotaFiscal(exchange);
        } else {
            sendResponse(exchange, 404, createErrorResponse("Endpoint não encontrado"));
        }
    }

    private void handlePostNotaFiscal(HttpExchange exchange) throws IOException {
        try {
            log.info("Recebida requisição para criar nova nota fiscal.");
            InputStream requestBody = exchange.getRequestBody();
            CriarNotaFiscalRequest requestDTO = objectMapper.readValue(requestBody, CriarNotaFiscalRequest.class);

            NotaFiscal notaFiscalCriada = notaFiscalService.criarNotaFiscal(requestDTO);

            String responseJson = objectMapper.writeValueAsString(notaFiscalCriada);
            sendResponse(exchange, 201, responseJson);
            log.info("Nota fiscal {} criada com sucesso.", notaFiscalCriada.id());

        } catch (IllegalArgumentException e) {
            log.warn("Requisição inválida: {}", e.getMessage());
            sendResponse(exchange, 400, createErrorResponse(e.getMessage()));
        } catch (PersistenciaException e) {
            log.error("Erro de persistência ao criar nota fiscal.", e);
            sendResponse(exchange, 500, createErrorResponse("Erro interno ao salvar a nota fiscal."));
        } catch (Exception e) {
            log.error("Erro inesperado ao processar a requisição.", e);
            sendResponse(exchange, 500, createErrorResponse("Erro inesperado no servidor."));
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String responseBody) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] responseBytes = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private String createErrorResponse(String message) {
        // Simples conversão manual para JSON para não falhar se o ObjectMapper tiver problemas.
        return "{\"error\":\"" + message.replace("\"", "'") + "\"}";
    }
}
