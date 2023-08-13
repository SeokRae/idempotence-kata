package com.example.idempotence.application.item.domain;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    private String id;
    private String name;
    private int quantity;

    public void setId(String id) { this.id = id; }

    public void setName(String name) { this.name = name; }

    public void setQuantity(int quantity) { this.quantity = quantity; }
}
