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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;

    @Transactional
    public Store createStore(CreateStoreRequest request, User currentUser) {
        validateProducerPermission(currentUser);

        if (storeRepository.findByStoreCode(request.getStoreCode()).isPresent()) {
            throw new BusinessLogicException("店舗コード '" + request.getStoreCode() + "' は既に使用されています");
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
    public List<Store> listStores() {
        return storeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Store getStore(Long id) {
        return storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store", id));
    }

    @Transactional
    public Store updateStore(Long id, UpdateStoreRequest request, User currentUser) {
        validateProducerPermission(currentUser);

        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store", id));

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

    private void validateProducerPermission(User user) {
        if (user.getRole() != Role.PRODUCER) {
            throw new ForbiddenException("店舗の登録・更新はPRODUCERのみ可能です");
        }
    }
}
