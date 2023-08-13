package com.example.idempotence.application.item.service;

import com.example.idempotence.application.item.domain.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class ItemService {

    // 아이템의 저장소로, 실제 애플리케이션에서는 데이터베이스에 대한 연결을 처리합니다.
    private final Map<String, Item> items;

    public ItemService() {
        items = new ConcurrentHashMap<>();
    }

    // 아이템 추가
    public Item addItem(Item item) {
        if (items.containsKey(item.getId())) {
            throw new IllegalArgumentException("Item with id " + item.getId() + " already exists");
        }

        items.put(item.getId(), item);
        return item;
    }

    // 수량 업데이트
    public Item updateItem(String itemId, String name, int quantity) {
        Item item = items.get(itemId);

        if (item == null) {
            item = new Item(itemId, name, quantity);
        } else {
            item.setName(name);
            item.setQuantity(quantity);
        }

        items.put(itemId, item);
        return item;
    }

    // 조회
    public Item getItem(String itemId) {
        return items.get(itemId);
    }

    // 전체 조회
    public List<Item> getAllItems() {
        return new ArrayList<>(items.values());
    }

    // 삭제
    public void removeItem(String id) {
        items.remove(id);
    }
}
