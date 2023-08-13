package com.example.idempotence.application.item.controller;

import com.example.idempotence.application.item.domain.Item;
import com.example.idempotence.application.item.service.ItemService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class IdempotentLocalCachedControllerDeleteTest {

    private final String IDEMPOTENCE_KEY = "unique-key-delete";
    private final String ITEM_ID = "1";
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
    @DisplayName("삭제 테스트")
    @Test
    public void deleteIdempotenceTest() throws Exception {

        mockMvc.perform(delete("/item/" + ITEM_ID)
                        .header("idempotency-key", IDEMPOTENCE_KEY))
                .andExpect(status().isOk())
                .andExpect(content().string("Deleted successfully"));
    }

    @Order(2)
    @DisplayName("멱등성 삭제 테스트")
    @Test
    void deleteIdempotenceDuplicateTest() throws Exception {

        mockMvc.perform(delete("/item/" + ITEM_ID)
                        .header("idempotency-key", IDEMPOTENCE_KEY))
                .andExpect(status().isOk())
                .andExpect(content().string("Request already processed"));
    }
}