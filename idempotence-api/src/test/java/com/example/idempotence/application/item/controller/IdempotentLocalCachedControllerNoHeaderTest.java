package com.example.idempotence.application.item.controller;

import com.example.idempotence.application.item.domain.Item;
import com.example.idempotence.application.item.service.ItemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class IdempotentLocalCachedControllerNoHeaderTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ItemService itemService;

    @BeforeEach
    void setUp() {
        itemService = new ItemService();

        // 테스트에 필요한 초기 아이템 추가
        itemService.addItem(new Item("1", "Item1", 10));
    }

    @DisplayName("헤더가 없는 경우에 대한 테스트")
    @Test
    public void missingHeaderTest() throws Exception {
        String itemId = "1";
        Item item = new Item(itemId, "Item1", 10);

        mockMvc.perform(put("/item/" + itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(item)))
                .andExpect(status().isBadRequest()); // 예를 들어, 잘못된 요청으로 처리할 수 있음
    }

}