package com.recipe.manager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recipe.manager.config.TestSecurityConfig;
import com.recipe.manager.dto.request.CreateStoreRequest;
import com.recipe.manager.dto.request.UpdateStoreRequest;
import com.recipe.manager.entity.Role;
import com.recipe.manager.entity.Store;
import com.recipe.manager.entity.User;
import com.recipe.manager.service.StoreService;
import com.recipe.manager.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StoreController.class)
@Import(TestSecurityConfig.class)
class StoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StoreService storeService;

    @MockitoBean
    private UserService userService;

    private static final LocalDateTime FIXED_TIME = LocalDateTime.of(2026, 1, 1, 0, 0);

    private static UsernamePasswordAuthenticationToken producerAuth() {
        return new UsernamePasswordAuthenticationToken(
                1L, "producer@example.com",
                List.of(new SimpleGrantedAuthority("ROLE_PRODUCER")));
    }

    private User createProducerUser() {
        return User.builder()
                .id(1L).email("producer@example.com").name("Producer").role(Role.PRODUCER)
                .enabled(true).createdAt(FIXED_TIME).updatedAt(FIXED_TIME)
                .build();
    }

    private Store createTestStore() {
        return Store.builder()
                .id(1L).storeCode("STORE001").name("テスト店舗").location("東京都渋谷区")
                .createdAt(FIXED_TIME).updatedAt(FIXED_TIME)
                .build();
    }

    @Test
    void 店舗作成_正常系_201が返る() throws Exception {
        User user = createProducerUser();
        Store store = createTestStore();

        when(userService.getUserById(1L)).thenReturn(user);
        when(storeService.createStore(any(CreateStoreRequest.class), eq(user))).thenReturn(store);

        CreateStoreRequest request = CreateStoreRequest.builder()
                .storeCode("STORE001").name("テスト店舗").location("東京都渋谷区").build();

        mockMvc.perform(post("/api/stores")
                        .with(authentication(producerAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.storeCode").value("STORE001"))
                .andExpect(jsonPath("$.name").value("テスト店舗"))
                .andExpect(jsonPath("$.location").value("東京都渋谷区"));
    }

    @Test
    void 店舗取得_正常系_200が返る() throws Exception {
        Store store = createTestStore();
        when(storeService.getStoreById(1L)).thenReturn(store);

        mockMvc.perform(get("/api/stores/1")
                        .with(authentication(producerAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storeCode").value("STORE001"));
    }

    @Test
    void 店舗一覧_正常系_200が返る() throws Exception {
        Store store = createTestStore();
        when(storeService.listStores(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(store)));

        mockMvc.perform(get("/api/stores")
                        .with(authentication(producerAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].storeCode").value("STORE001"));
    }

    @Test
    void 店舗更新_正常系_200が返る() throws Exception {
        User user = createProducerUser();
        Store updated = createTestStore();
        updated.setName("更新店舗");

        when(userService.getUserById(1L)).thenReturn(user);
        when(storeService.updateStore(eq(1L), any(UpdateStoreRequest.class), eq(user))).thenReturn(updated);

        UpdateStoreRequest request = UpdateStoreRequest.builder().name("更新店舗").build();

        mockMvc.perform(put("/api/stores/1")
                        .with(authentication(producerAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("更新店舗"));
    }

    @Test
    void 認証なし_401が返る() throws Exception {
        mockMvc.perform(get("/api/stores"))
                .andExpect(status().isUnauthorized());
    }
}
