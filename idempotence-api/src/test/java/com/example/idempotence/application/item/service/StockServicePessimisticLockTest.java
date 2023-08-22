package com.example.idempotence.application.item.service;

import com.example.idempotence.application.item.domain.Id;
import com.example.idempotence.application.item.domain.Stock;
import com.example.idempotence.application.item.domain.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class StockServicePessimisticLockTest {

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

    @DisplayName("Pessimistic lock test")
    @Test
    public void testIncreasePessimisticLockMultiThreadTest() throws InterruptedException {
        int threads = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    stockService.increasePessimisticLockQuantity(stockId, 1);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();  // Wait for all threads to finish

        Optional<Stock> finalStock = stockRepository.findPessimisticLockById(stockId);

        // If each thread reduces the quantity by 10, and there are 10 threads, the final quantity should be 0.
        assertThat(finalStock).isPresent()
                .hasValueSatisfying(stock -> assertThat(stock.getQuantity()).isGreaterThan(100));
    }
}