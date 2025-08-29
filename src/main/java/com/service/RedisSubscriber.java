package com.service;


import com.dto.ClienteDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.JedisPubSub;

@Slf4j
public class RedisSubscriber extends JedisPubSub {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ClienteCacheService cacheService;

    public RedisSubscriber(ClienteCacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Override
    public void onMessage(String channel, String message) {
        log.info("Mensagem recebida no canal '{}'", channel);
        try {
            ClienteDTO cliente = objectMapper.readValue(message, ClienteDTO.class);

            cacheService.cacheCliente(cliente);

        } catch (Exception e) {
            log.error("Falha ao processar mensagem do canal '{}'.", channel, e);
        }
    }
}