package com.example.idempotence.application.item.service;

import com.example.idempotence.application.item.domain.Id;
import com.example.idempotence.application.item.domain.Stock;
import com.example.idempotence.application.item.domain.StockRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

@SpringBootTest
@Transactional
class StockServiceTest {

    @Autowired
    private StockService stockService;

    @Autowired
    private StockRepository stockRepository;

    @Test
    public void testCreateStock() {
        // given
        Stock stock = new Stock(new Id<>(1L), "test-item", 5);
        // when
        Stock savedStock = stockService.createStock(stock);
        // then
        assertThat(savedStock.getName()).isEqualTo(stock.getName());
    }

    @Test
    public void testGetStock() {
        // given
        Stock stock = new Stock(new Id<>(2L), "test-item-2", 10);
        // when
        stockRepository.save(stock);
        Optional<Stock> retrievedStock = stockService.getStock(new Id<>(2L));
        // then
        assertThat(retrievedStock).isPresent()
                .hasValueSatisfying(s ->
                        assertThat(s.getName()).isEqualTo("test-item-2")
                );
    }

    @Test
    public void testUpdateStock() {
        // given
        Stock stock = new Stock(new Id<>(3L), "test-item-3", 15);
        // when
        stockRepository.save(stock);
        stock.changeName("updated-item");
        Optional<Stock> updatedStock = stockService.updateStock(new Id<>(3L), stock);
        // then
        assertThat(updatedStock).isPresent();
        assertThat(updatedStock.get().getName()).isEqualTo("updated-item");
    }

    @Test
    public void testDeleteStock() {
        // given
        Stock stock = new Stock(new Id<>(4L), "test-item-4", 20);
        // when
        stockRepository.save(stock);
        stockService.deleteStock(new Id<>(4L));
        // then
        assertThat(stockRepository.existsById(new Id<>(4L))).isFalse();
    }

    @Test
    public void testChangeQuantity() {
        // given
        Stock stock = new Stock(new Id<>(5L), "test-item-5", 100);
        // when
        stockRepository.save(stock);
        // then
        assertThat(stockService.decreaseQuantity(new Id<>(5L), 50))
                .isEqualTo(new Stock(new Id<>(5L), "test-item-5", 50));
        assertThat(stockRepository.findPessimisticLockById(new Id<>(5L))).isPresent()
                        .hasValueSatisfying(stock1 -> assertThat(stock1.getQuantity()).isEqualTo(50));

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> stockService.decreaseQuantity(new Id<>(5L), 200))
                .withMessageContaining("재고가 부족합니다.");
    }
}