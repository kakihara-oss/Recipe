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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;

    @Transactional
    public Store createStore(CreateStoreRequest request, User currentUser) {
        validateStoreManagePermission(currentUser);

        if (storeRepository.existsByStoreCode(request.getStoreCode())) {
            throw new BusinessLogicException("同じ店舗コードが既に登録されています: " + request.getStoreCode());
        }

        Store store = Store.builder()
                .storeCode(request.getStoreCode())
                .name(request.getName())
                .location(request.getLocation())
                .build();

        Store saved = storeRepository.save(store);
        log.info("Store created: id={}, code={}, by={}", saved.getId(), saved.getStoreCode(), currentUser.getEmail());
        return saved;
    }

    @Transactional(readOnly = true)
    public Store getStoreById(Long id) {
        return storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store", id));
    }

    @Transactional(readOnly = true)
    public Page<Store> listStores(Pageable pageable) {
        return storeRepository.findAll(pageable);
    }

    @Transactional
    public Store updateStore(Long id, UpdateStoreRequest request, User currentUser) {
        validateStoreManagePermission(currentUser);

        Store store = getStoreById(id);

        if (request.getName() != null) {
            store.setName(request.getName());
        }
        if (request.getLocation() != null) {
            store.setLocation(request.getLocation());
        }

        Store saved = storeRepository.save(store);
        log.info("Store updated: id={}, by={}", id, currentUser.getEmail());
        return saved;
    }

    private void validateStoreManagePermission(User user) {
        if (user.getRole() != Role.PRODUCER) {
            throw new ForbiddenException("店舗マスタの管理権限がありません");
        }
    }
}
