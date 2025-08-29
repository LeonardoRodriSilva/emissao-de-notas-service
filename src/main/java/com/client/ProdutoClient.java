package com.client;

import com.dto.ProdutoDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Slf4j
public class ProdutoClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String produtosApiUrl;

    public ProdutoClient(ObjectMapper objectMapper) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = objectMapper;
        this.produtosApiUrl = "http://localhost:8080/api/produtos";
    }

    public ProdutoDTO buscarProdutoPorId(String produtoId) {
        log.info("Buscando produto com ID {} na API de cadastros...", produtoId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(produtosApiUrl + "/" + produtoId))
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ProdutoDTO produto = objectMapper.readValue(response.body(), ProdutoDTO.class);
                log.info("Produto encontrado: {}", produto.nome());
                return produto;
            } else {
                log.error("Erro ao buscar produto ID {}. Status Code: {}", produtoId, response.statusCode());
                return null;
            }
        } catch (IOException | InterruptedException e) {
            log.error("Erro de comunicação ao buscar produto ID {}", produtoId, e);
            Thread.currentThread().interrupt();
            return null;
        }
    }
}