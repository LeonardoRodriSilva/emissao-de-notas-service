package com;

import com.client.ProdutoClient;
import com.config.RedisConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.handler.NotaFiscalHttpHandler;
import com.repository.NotaFiscalRepository;
import com.service.ClienteCacheService;
import com.service.NotaFiscalService;
import com.service.RedisListenerManager;
import com.sun.net.httpserver.HttpServer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;

@Slf4j
public class AplicacaoNotaFiscais {

    private static final int HTTP_PORT = 8082;

    public static void main(String[] args) throws IOException {
        log.info("Iniciando Serviço de Nota Fiscal.");

        ObjectMapper objectMapper = new ObjectMapper();
        NotaFiscalRepository notaFiscalRepository = new NotaFiscalRepository();
        ClienteCacheService clienteCacheService = new ClienteCacheService();
        ProdutoClient produtoClient = new ProdutoClient(objectMapper); // Assumindo o nome ProdutoClient

        NotaFiscalService notaFiscalService = new NotaFiscalService(
                notaFiscalRepository,
                clienteCacheService,
                produtoClient
        );

        final RedisListenerManager listenerManager = new RedisListenerManager(clienteCacheService);
        listenerManager.start();

        HttpServer server = HttpServer.create(new InetSocketAddress(HTTP_PORT), 0);
        server.createContext("/api/notas-fiscais", new NotaFiscalHttpHandler(notaFiscalService, objectMapper));
        server.setExecutor(null);
        server.start();
        log.info("Servidor HTTP iniciado na porta {}.", HTTP_PORT);

        log.info("Serviço de Nota Fiscal no ar. Pressione Ctrl+C para parar.");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Desligando Serviço de Nota Fiscal.");
            server.stop(0);
            listenerManager.stop();
            RedisConfig.close();
            log.info("Recursos liberados. Serviço desligado.");
        }));
    }
}