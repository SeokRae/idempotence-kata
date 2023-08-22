package com.example.idempotence.application.item.service;

import com.example.idempotence.application.item.domain.Id;
import com.example.idempotence.application.item.domain.Stock;
import com.example.idempotence.application.item.domain.StockRepository;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

@SpringBootTest
public class StockServiceOptimisticLockExceptionTest {

    @Autowired
    private StockService stockService;

    @Autowired
    private StockRepository stockRepository;

    private Id<Long> stockId;

    @BeforeEach
    public void setUp() {
        Stock initialStock = new Stock(new Id<>(1L), "test-item", 0);
        Stock savedStock = stockRepository.save(initialStock);
        stockId = savedStock.getId();
    }
    @Test
    public void testOptimisticLockException() throws InterruptedException {
        int threads = 100;
        ExecutorService executor = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger exceptionCount = new AtomicInteger();

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    stockService.increaseOptimisticLockQuantity(stockId, 1);
                } catch (OptimisticLockException | ObjectOptimisticLockingFailureException e) {
                    exceptionCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        // 여러 스레드에서 경쟁 조건 때문에 적어도 한 번 이상의 예외가 발생함을 검증
        assertThat(exceptionCount.get()).isGreaterThan(0);
    }
}