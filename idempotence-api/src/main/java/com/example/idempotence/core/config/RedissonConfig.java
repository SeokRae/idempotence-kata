package com.example.idempotence.core.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.redisson.config.TransportMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Bean
    public Config config() {
        Config config = new Config();

        config.setThreads(16);
        config.setNettyThreads(32);
        config.setCodec(new JsonJacksonCodec());
        config.setTransportMode(TransportMode.NIO);

        SingleServerConfig singleServerConfig = config.useSingleServer();
        singleServerConfig.setIdleConnectionTimeout(10000);
        singleServerConfig.setConnectTimeout(10000);
        singleServerConfig.setTimeout(3000);
        singleServerConfig.setRetryAttempts(3);
        singleServerConfig.setRetryInterval(1500);
        singleServerConfig.setPassword(null);
        singleServerConfig.setSubscriptionsPerConnection(5);
        singleServerConfig.setClientName(null);
        singleServerConfig.setAddress("redis://127.0.0.1:6379");
        singleServerConfig.setSubscriptionConnectionMinimumIdleSize(1);
        singleServerConfig.setSubscriptionConnectionPoolSize(50);
        singleServerConfig.setConnectionMinimumIdleSize(10);
        singleServerConfig.setConnectionPoolSize(64);
        singleServerConfig.setDatabase(0);
        singleServerConfig.setDnsMonitoringInterval(5000);

        return config;
    }

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient(Config config) {
        return Redisson.create(config);
    }
}