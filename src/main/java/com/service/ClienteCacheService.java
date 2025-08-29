package com.service;

import com.dto.ClienteDTO;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ClienteCacheService {

    private final Map<Long, ClienteDTO> cache = new ConcurrentHashMap<>();


    public void cacheCliente(ClienteDTO cliente) {
        if (cliente == null || cliente.id() == null) {
            log.warn("Tentativa de cachear um cliente nulo ou sem ID.");
            return;
        }
        cache.put(cliente.id(), cliente);
        log.info("Cliente ID {} ('{}') salvo/atualizado no cache. Total em cache: {}",
                cliente.id(), cliente.nome(), cache.size());
    }

    public Optional<ClienteDTO> getCliente(Long id) {
        log.debug("Buscando cliente ID {} no cache.", id);
        return Optional.ofNullable(cache.get(id));
    }
}