package com.example.idempotence.application.item.controller;

import com.example.idempotence.application.item.domain.Item;
import com.example.idempotence.application.item.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.hamcrest.core.StringContains.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
class IdempotentLocalCachedControllerDecreaseLoadTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ItemService itemService;

    @BeforeEach
    void setUp() {
        // 초기 아이템 추가 (수량 0으로 시작)
        Item item = new Item("multithread-item", "Multithread Item", 100);
        itemService.addItem(item);

    }

    @DisplayName("부하 테스트 - 수량 감소")
    @Test
    public void multiThreadedDecrementTest() throws Exception {
        int numberOfThreads = 100;
        int decrementBy = 1;
        String itemId = "multithread-item";
        String idempotencyKeyPrefix = "key-";

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        for (int i = 0; i < numberOfThreads; i++) {
            int finalI = i;
            executor.submit(() -> {
                try {
                    mockMvc.perform(put("/item/" + itemId + "/decrement")
                                    .header("idempotency-key", idempotencyKeyPrefix + finalI)
                                    .param("decrementBy", String.valueOf(decrementBy))
                                    .contentType(MediaType.APPLICATION_JSON))
                            .andExpect(status().isOk())
                            .andExpect(content().string(containsString("Updated successfully")));
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        // 모든 스레드가 완료될 때까지 기다림
        latch.await();

        // 최종적으로 생성된 데이터의 개수 확인
        mockMvc.perform(get("/item/" + itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(0));
    }

    @DisplayName("부하 테스트 - 수량 감소")
    @Test
    public void multiThreadedIncrementTest() throws Exception {
        int numberOfThreads = 100;
        int incrementBy = 1;
        String itemId = "multithread-item";
        String idempotencyKeyPrefix = "key-";

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        for (int i = 0; i < numberOfThreads; i++) {
            int finalI = i;
            executor.submit(() -> {
                try {
                    mockMvc.perform(put("/item/" + itemId + "/increment")
                                    .header("idempotency-key", idempotencyKeyPrefix + finalI)
                                    .param("incrementBy", String.valueOf(incrementBy))
                                    .contentType(MediaType.APPLICATION_JSON))
                            .andExpect(status().isOk())
                            .andExpect(content().string(containsString("Updated successfully")));
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        // 모든 스레드가 완료될 때까지 기다림
        latch.await();

        // 최종적으로 생성된 데이터의 개수 확인
        mockMvc.perform(get("/item/" + itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(numberOfThreads));
    }

}