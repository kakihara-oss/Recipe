package com.recipe.manager.service;

import com.recipe.manager.dto.request.CreateStoreRequest;
import com.recipe.manager.dto.request.UpdateStoreRequest;
import com.recipe.manager.entity.Role;
import com.recipe.manager.entity.Store;
import com.recipe.manager.entity.User;
import com.recipe.manager.exception.BusinessLogicException;
import com.recipe.manager.exception.ForbiddenException;
import com.recipe.manager.exception.ResourceNotFoundException;
import com.recipe.manager.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private StoreService storeService;

    private User producerUser;
    private User chefUser;

    @BeforeEach
    void setUp() {
        producerUser = User.builder().id(1L).email("producer@example.com").name("Producer").role(Role.PRODUCER).build();
        chefUser = User.builder().id(2L).email("chef@example.com").name("Chef").role(Role.CHEF).build();
    }

    private Store createTestStore() {
        return Store.builder()
                .id(1L)
                .storeCode("STORE001")
                .name("テスト店舗")
                .location("東京都渋谷区")
                .build();
    }

    @Test
    void 店舗作成_正常系_プロデューサーが作成できる() {
        CreateStoreRequest request = CreateStoreRequest.builder()
                .storeCode("STORE001")
                .name("テスト店舗")
                .location("東京都渋谷区")
                .build();

        when(storeRepository.existsByStoreCode("STORE001")).thenReturn(false);
        when(storeRepository.save(any(Store.class))).thenAnswer(invocation -> {
            Store saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        Store result = storeService.createStore(request, producerUser);

        assertNotNull(result);
        assertEquals("STORE001", result.getStoreCode());
        assertEquals("テスト店舗", result.getName());
        assertEquals("東京都渋谷区", result.getLocation());
    }

    @Test
    void 店舗作成_異常系_シェフは作成できない() {
        CreateStoreRequest request = CreateStoreRequest.builder()
                .storeCode("STORE001")
                .name("テスト店舗")
                .build();

        assertThrows(ForbiddenException.class, () ->
                storeService.createStore(request, chefUser));
    }

    @Test
    void 店舗作成_異常系_同じ店舗コードが既に存在する() {
        CreateStoreRequest request = CreateStoreRequest.builder()
                .storeCode("STORE001")
                .name("テスト店舗")
                .build();

        when(storeRepository.existsByStoreCode("STORE001")).thenReturn(true);

        assertThrows(BusinessLogicException.class, () ->
                storeService.createStore(request, producerUser));
    }

    @Test
    void 店舗取得_正常系_IDで取得できる() {
        Store store = createTestStore();
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));

        Store result = storeService.getStoreById(1L);

        assertEquals("テスト店舗", result.getName());
    }

    @Test
    void 店舗取得_異常系_存在しないID() {
        when(storeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                storeService.getStoreById(999L));
    }

    @Test
    void 店舗更新_正常系_プロデューサーが更新できる() {
        Store store = createTestStore();
        UpdateStoreRequest request = UpdateStoreRequest.builder()
                .name("更新店舗")
                .location("東京都新宿区")
                .build();

        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(storeRepository.save(any(Store.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Store result = storeService.updateStore(1L, request, producerUser);

        assertEquals("更新店舗", result.getName());
        assertEquals("東京都新宿区", result.getLocation());
    }
}
