package com.example.idempotence.application.item.controller;

import com.example.idempotence.application.item.domain.Item;
import com.example.idempotence.application.item.service.ItemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IdempotentLocalCachedControllerPutTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ItemService itemService;

    @BeforeEach
    void setUp() {
        itemService = new ItemService();

        // 테스트에 필요한 초기 아이템 추가
        itemService.addItem(new Item("1", "Item1", 10));
        itemService.addItem(new Item("2", "Item2", 5));
    }

    @Order(1)
    @DisplayName("동일한 헤더로 요청에 대한 멱등성 테스트")
    @Test
    public void idempotenceConfirmTest() throws Exception {
        // given
        String idempotencyKey = "unique-key";
        String itemId = "1";
        Item item = new Item(itemId, "Item1", 10);

        // when
        mockMvc.perform(put("/item/" + itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("idempotency-key", idempotencyKey)
                        .content(new ObjectMapper().writeValueAsString(item)))
                .andExpect(status().isOk())
                .andExpect(content().string("Updated successfully"));

    }

    @Order(2)
    @DisplayName("중복 요청에 대한 멱등성 테스트")
    @Test
    void idempotenceDuplicateRequestTest() throws Exception {
        String idempotencyKey = "unique-key";
        String itemId = "1";
        Item item = new Item(itemId, "Item1", 10);
        // 동일한 idempotencyKey로 다시 시도
        mockMvc.perform(put("/item/" + itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("idempotency-key", idempotencyKey)
                        .content(new ObjectMapper().writeValueAsString(item)))
                .andExpect(status().isOk())
                .andExpect(content().string("Request already processed"));
    }
}
