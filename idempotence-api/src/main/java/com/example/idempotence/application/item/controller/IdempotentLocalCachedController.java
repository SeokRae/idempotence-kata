package com.example.idempotence.application.item.controller;

import com.example.idempotence.application.item.domain.Item;
import com.example.idempotence.application.item.service.ItemService;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class IdempotentLocalCachedController {

    private final ItemService itemService;
    private final Cache<String, Boolean> idempotencyKeys;

    @GetMapping("/items")
    public ResponseEntity<List<Item>> getAllItems() {
        log.info("조회 요청");
        List<Item> items = itemService.getAllItems();
        log.info("조회 결과: {}", items);
        return ResponseEntity.ok().body(items);
    }

    @GetMapping("/item/{id}")
    public ResponseEntity<Item> getItem(@PathVariable String id) {
        Item item = itemService.getItem(id);
        if (item == null) {
            log.warn("조회 실패: {}", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        log.info("조회 성공: {}", item);
        return ResponseEntity.ok().body(item);
    }

    @PutMapping("/item/{id}")
    public ResponseEntity<String> updateItem(
            @PathVariable String id,
            @RequestBody Item itemRequest,
            @RequestHeader("idempotency-key") String idempotencyKey
    ) {
        if (idempotencyKeys.getIfPresent(idempotencyKey) != null) {
            log.warn("이미 처리된 요청: {}", idempotencyKey);
            return ResponseEntity.ok().body("Request already processed");
        }

        Item updatedItem = itemService.updateItem(
                id, itemRequest.getName(), itemRequest.getQuantity()
        );
        if (updatedItem == null) {
            log.warn("수정 실패: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Item not found");
        }

        idempotencyKeys.put(idempotencyKey, true);

        log.info("수정 성공: {}", updatedItem);
        return ResponseEntity.ok()
                .body("Updated successfully");
    }

    // 수량 감소
    @PutMapping("/item/{itemId}/decrement")
    public ResponseEntity<String> decrementItemQuantity(
            @PathVariable String itemId,
            @RequestHeader("idempotency-key") String idempotencyKey,
            @RequestParam int decrementBy
    ) {
        if (idempotencyKeys.getIfPresent(idempotencyKey) != null) {
            log.warn("이미 처리된 요청: {}", idempotencyKey);
            return ResponseEntity.ok("Request already processed");
        }

        try {
            Item item = itemService.decrementQuantity(itemId, decrementBy);
            idempotencyKeys.put(idempotencyKey, true);

            log.info("수량 감소 성공: {}", item);
            return ResponseEntity.ok("Updated successfully, new quantity: " + item.getQuantity());
        } catch (IllegalArgumentException e) {
            log.warn("수량 감소 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/item/{itemId}/increment")
    public ResponseEntity<String> incrementItemQuantity(
            @PathVariable String itemId,
            @RequestHeader("idempotency-key") String idempotencyKey,
            @RequestParam int incrementBy
    ) {
        if (idempotencyKeys.getIfPresent(idempotencyKey) != null) {
            log.warn("이미 처리된 요청: {}", idempotencyKey);
            return ResponseEntity.ok("Request already processed");
        }

        try {
            Item item = itemService.incrementQuantity(itemId, incrementBy);
            idempotencyKeys.put(idempotencyKey, true);

            log.info("수량 증가 성공: {}", item);
            return ResponseEntity.ok("Updated successfully, new quantity: " + item.getQuantity());
        } catch (IllegalArgumentException e) {
            log.warn("수량 증가 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/item/{id}")
    public ResponseEntity<String> deleteItem(
            @PathVariable String id,
            @RequestHeader("idempotency-key") String idempotencyKey
    ) {
        if (idempotencyKeys.getIfPresent(idempotencyKey) != null) {
            log.warn("이미 처리된 요청: {}", idempotencyKey);
            return new ResponseEntity<>("Request already processed", HttpStatus.OK);
        }

        itemService.removeItem(id);
        idempotencyKeys.put(idempotencyKey, true);

        log.info("삭제 성공: {}", id);
        return ResponseEntity.ok().body("Deleted successfully");
    }
}