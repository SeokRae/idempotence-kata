package com.example.idempotence.application.item.controller;

import com.example.idempotence.application.item.domain.Item;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class IdempotentLocalCachedControllerLoadTest {
    @Autowired
    private MockMvc mockMvc;

    @DisplayName("부하 테스트")
    @Test
    public void multiThreadedCreationTest() throws Exception {
        int numberOfThreads = 10;
        String itemId = "multithread-item";
        String idempotencyKeyPrefix = "key-";

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        for (int i = 0; i < numberOfThreads; i++) {
            int finalI = i;
            executor.submit(() -> {
                try {
                    Item item = new Item(itemId, "Multithread Item", finalI);
                    mockMvc.perform(put("/item/" + itemId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .header("idempotency-key", idempotencyKeyPrefix + finalI)
                                    .content(new ObjectMapper().writeValueAsString(item)))
                            .andExpect(status().isOk())
                            .andExpect(content().string("Updated successfully"));
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
        // 데이터의 실제 유형과 구조에 따라 검증 로직 조정
        mockMvc.perform(get("/item/" + itemId)) // 아이템 조회 API 경로
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(numberOfThreads));
    }
}