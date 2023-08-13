package com.example.idempotence.core.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public Cache<String, Boolean> idempotencyKeys() {
        return Caffeine.newBuilder()
                .expireAfterWrite(24, TimeUnit.HOURS) // 24시간 후에 만료
                .maximumSize(1000) // 최대 크기 설정
                .build();
    }
}