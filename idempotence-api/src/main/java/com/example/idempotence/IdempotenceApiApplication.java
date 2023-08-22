package com.example.idempotence;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
public class IdempotenceApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdempotenceApiApplication.class, args);
    }

}
