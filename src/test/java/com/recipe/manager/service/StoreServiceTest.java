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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private StoreService storeService;

    private User createUser(Role role) {
        return User.builder().id(1L).email("test@example.com").role(role).build();
    }

    @Test
    void 店舗作成_正常系_PRODUCERが作成できる() {
        CreateStoreRequest request = new CreateStoreRequest();
        request.setStoreCode("STORE001");
        request.setName("本店");
        request.setLocation("東京都");

        when(storeRepository.findByStoreCode("STORE001")).thenReturn(Optional.empty());
        when(storeRepository.save(any(Store.class))).thenAnswer(inv -> {
            Store s = inv.getArgument(0);
            s.setId(1L);
            return s;
        });

        Store result = storeService.createStore(request, createUser(Role.PRODUCER));
        assertThat(result.getStoreCode()).isEqualTo("STORE001");
        assertThat(result.getName()).isEqualTo("本店");
    }

    @Test
    void 店舗作成_異常系_CHEF権限では作成できない() {
        CreateStoreRequest request = new CreateStoreRequest();
        request.setStoreCode("STORE001");
        request.setName("本店");

        assertThatThrownBy(() -> storeService.createStore(request, createUser(Role.CHEF)))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void 店舗作成_異常系_重複コードはエラー() {
        CreateStoreRequest request = new CreateStoreRequest();
        request.setStoreCode("STORE001");
        request.setName("本店");

        when(storeRepository.findByStoreCode("STORE001")).thenReturn(Optional.of(new Store()));

        assertThatThrownBy(() -> storeService.createStore(request, createUser(Role.PRODUCER)))
                .isInstanceOf(BusinessLogicException.class);
    }

    @Test
    void 店舗一覧_正常系_全件取得できる() {
        when(storeRepository.findAll()).thenReturn(List.of(
                Store.builder().id(1L).storeCode("S1").name("本店").build(),
                Store.builder().id(2L).storeCode("S2").name("支店").build()
        ));

        List<Store> stores = storeService.listStores();
        assertThat(stores).hasSize(2);
    }

    @Test
    void 店舗取得_異常系_存在しないIDはエラー() {
        when(storeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> storeService.getStore(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void 店舗更新_正常系_名前を更新できる() {
        Store existing = Store.builder().id(1L).storeCode("S1").name("旧名").build();
        when(storeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(storeRepository.save(any(Store.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateStoreRequest request = new UpdateStoreRequest();
        request.setName("新名");

        Store result = storeService.updateStore(1L, request, createUser(Role.PRODUCER));
        assertThat(result.getName()).isEqualTo("新名");
    }
}
