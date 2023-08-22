package com.example.idempotence.application.item.service;

import com.example.idempotence.application.item.domain.Id;
import com.example.idempotence.application.item.domain.Stock;
import com.example.idempotence.application.item.domain.StockRepository;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class StockServiceOptimisticLockRetryTest {


    @MockBean
    private StockRepository stockRepository;

    @Autowired
    private StockService stockService;  // Assuming your service class name is StockService

    @Test
    public void testRetryOnOptimisticLock() {
        // Given
        Id<Long> id = new Id<>(1L);

        when(stockRepository.findOptimisticLockById(id))
                .thenThrow(OptimisticLockingFailureException.class);

        // When
        assertThatExceptionOfType(OptimisticLockingFailureException.class)
                .isThrownBy(() -> stockService.increaseOptimisticLockRetryQuantity(id, 1));

        // Then
        verify(stockRepository, times(3)).findOptimisticLockById(id);
    }
}