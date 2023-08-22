package com.example.idempotence.application.item.domain;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

import static jakarta.persistence.LockModeType.OPTIMISTIC;
import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;

public interface StockRepository extends JpaRepository<Stock, Id<Long>> {
    @Lock(PESSIMISTIC_WRITE)
    Optional<Stock> findPessimisticLockById(@NotNull Id<Long> id);
    @Lock(OPTIMISTIC)
    Optional<Stock> findOptimisticLockById(@NotNull Id<Long> id);
}
