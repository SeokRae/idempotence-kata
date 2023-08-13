package com.example.idempotence.application.item.controller;

import com.example.idempotence.application.item.domain.Item;
import com.example.idempotence.application.item.service.ItemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class IdempotentLocalCachedControllerUnitTest {

    @InjectMocks
    private IdempotentLocalCachedController idempotentLocalCachedController;

    @Mock
    private ItemService itemService;

    @Mock
    private Cache<String, Boolean> idempotencyKeys;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(idempotentLocalCachedController).build();
    }

    @DisplayName("아이템 업데이트 단위 테스트")
    @Test
    public void testUpdateItem() throws Exception {
        String idempotencyKey = "unique-key";
        String itemId = "1";
        Item item = new Item(itemId, "Item1", 10);

        when(idempotencyKeys.getIfPresent(idempotencyKey)).thenReturn(null);
        when(itemService.updateItem(anyString(), anyString(), anyInt())).thenReturn(item);

        mockMvc.perform(put("/item/" + itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("idempotency-key", idempotencyKey)
                        .content(new ObjectMapper().writeValueAsString(item)))
                .andExpect(status().isOk())
                .andExpect(content().string("Updated successfully"));
    }
}
