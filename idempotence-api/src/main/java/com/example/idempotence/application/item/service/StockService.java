package com.example.idempotence.application.item.service;

import com.example.idempotence.application.item.domain.Id;
import com.example.idempotence.application.item.domain.Stock;
import com.example.idempotence.application.item.domain.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.dialect.lock.OptimisticEntityLockException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;

import java.net.SocketTimeoutException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;

    public Stock createStock(Stock stock) {
        return stockRepository.save(stock);
    }

    public Optional<Stock> getStock(Id<Long> id) {
        return stockRepository.findPessimisticLockById(id);
    }

    @Transactional
    public Optional<Stock> updateStock(Id<Long> id, Stock stock) {
        if (stockRepository.existsById(id)) {
            return Optional.of(stockRepository.save(stock));
        }
        return Optional.empty();
    }

    public void deleteStock(Id<Long> id) {
        stockRepository.deleteById(id);
    }

    @Transactional
    public Stock decreaseQuantity(Id<Long> id, int quantityChange) {
        log.info("changeQuantity: id={}, quantityChange={}", id, quantityChange);

        return stockRepository.findPessimisticLockById(id)
                .map(stock -> stock.decreaseQuantity(quantityChange))
                .map(stockRepository::save)
                .orElseThrow(() -> new IllegalArgumentException("Stock not found"));
    }

    // 비관적 락은 요청에 대한 일관성 및 정합성, 동시성을 보장하지만, 성능이 떨어진다.
    @Transactional
    public void increasePessimisticLockQuantity(Id<Long> id, int quantityChange) {
        log.info("changeQuantity: id={}, quantityChange={}", id, quantityChange);

        stockRepository.findPessimisticLockById(id)
                .map(stock -> stock.increaseQuantity(quantityChange))
                .orElseThrow(() -> new IllegalArgumentException("Stock not found"));
    }

    // 낙관적 락은 동시성 해결하기 위한 한 가지 방법일 뿐, 모든 상황에서 동시성을 보장하진 않는다.
    @Transactional
    public void increaseOptimisticLockQuantity(Id<Long> id, int quantityChange) {
        log.info("changeQuantity: id={}, quantityChange={}", id, quantityChange);

        stockRepository.findOptimisticLockById(id)
                .map(stock -> stock.increaseQuantity(quantityChange))
                .orElseThrow(() -> new IllegalArgumentException("Stock not found"));
    }

    @Retryable(
            retryFor = {OptimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000)
    )
    @Transactional
    public void increaseOptimisticLockRetryQuantity(Id<Long> id, int quantityChange) {
        log.info("changeQuantity: id={}, quantityChange={}", id, quantityChange);

        stockRepository.findOptimisticLockById(id)
                .map(stock -> stock.increaseQuantity(quantityChange))
                .map(stockRepository::save)
                .orElseThrow(() -> new IllegalArgumentException("Stock not found"));
    }
}
