package com.example.idempotence.application.item.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
@Entity
@Getter
@ToString
@NoArgsConstructor
public class Stock {

    @EmbeddedId
    @AttributeOverride(name="value", column=@Column(name="stock_id"))
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Id<Long> id;
    private String name;
    private int quantity;

    @Version
    private Long version;
    @Builder
    public Stock(Id<Long> id, String name, int quantity) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
    }

    public void changeName(String name) {
        this.name = name;
    }

    public Stock decreaseQuantity(int quantityChange) {
        int remainStock = this.quantity - quantityChange;
        log.info("pre {}", this);
        if (remainStock < 0) {
            throw new IllegalArgumentException("재고가 부족합니다.");
        }
        this.quantity = remainStock;
        log.info("post Stock {}", this);
        return this;
    }

    public Stock increaseQuantity(int quantityChange) {
        this.quantity += quantityChange;
        log.info("post Stock {}", this);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Stock stock)) return false;
        return Objects.equals(id, stock.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
