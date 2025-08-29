package com;

import com.config.RedisConfig;
import com.service.RedisListenerManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AplicacaoNotaFiscal {

    public static void main(String[] args) {
        log.info("Iniciando Serviço de Nota Fiscal.");

        final RedisListenerManager listenerManager = new RedisListenerManager();
        listenerManager.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Desligando Serviço de Nota Fiscal.");
            listenerManager.stop();
            RedisConfig.close();
            log.info("Recursos liberados. Serviço desligado.");
        }));
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            log.warn("Thread principal interrompida.");
            Thread.currentThread().interrupt();
        }
    }
}