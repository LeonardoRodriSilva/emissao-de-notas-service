package com.service;


import com.config.RedisConfig;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

@Slf4j
public class RedisListenerManager {

    private final Thread subscriberThread;
    private final RedisSubscriber subscriber;
    private static final String CHANNEL_NAME = "clientes-topic";

    public RedisListenerManager(ClienteCacheService cacheService) {
        this.subscriber = new RedisSubscriber(cacheService);
        this.subscriberThread = new Thread(() -> {
            try (Jedis jedis = RedisConfig.getJedis()) {
                log.info("Subscriber conectado. Ouvindo canal '{}'...", CHANNEL_NAME);
                jedis.subscribe(subscriber, CHANNEL_NAME);
            } catch (Exception e) {
                log.error("Conexão do subscriber Redis perdida.", e);
            }
            log.info("Subscriber do canal '{}' foi encerrado.", CHANNEL_NAME);
        }, "Redis-Subscriber-Thread");
        this.subscriberThread.setDaemon(true);
    }

    public void start() {
        log.info("Iniciando Redis Listener em segundo plano.");
        subscriberThread.start();
    }

    public void stop() {
        log.info("Solicitando parada do subscriber Redis...");
        if (subscriber.isSubscribed()) {
            subscriber.unsubscribe();
        }
    }
}
