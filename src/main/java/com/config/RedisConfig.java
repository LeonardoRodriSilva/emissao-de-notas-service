package com.config;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisConfig {

    private static final JedisPool jedisPool;

    static {
        String host = "localhost";
        int port = 6379;
        jedisPool = new JedisPool(new JedisPoolConfig(), host, port);
    }

    public static Jedis getJedis() {
        return jedisPool.getResource();
    }

    public static void close() {
        if (jedisPool != null) {
            jedisPool.close();
        }
    }
}
