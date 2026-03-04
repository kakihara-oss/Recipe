package com.recipe.manager.controller;

import com.recipe.manager.dto.request.CreateStoreRequest;
import com.recipe.manager.dto.request.UpdateStoreRequest;
import com.recipe.manager.dto.response.StoreResponse;
import com.recipe.manager.entity.Store;
import com.recipe.manager.entity.User;
import com.recipe.manager.service.StoreService;
import com.recipe.manager.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<StoreResponse> createStore(
            @Valid @RequestBody CreateStoreRequest request,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Store store = storeService.createStore(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(StoreResponse.from(store));
    }

    @GetMapping
    public ResponseEntity<List<StoreResponse>> listStores() {
        List<Store> stores = storeService.listStores();
        List<StoreResponse> response = stores.stream()
                .map(StoreResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreResponse> getStore(@PathVariable Long id) {
        Store store = storeService.getStore(id);
        return ResponseEntity.ok(StoreResponse.from(store));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StoreResponse> updateStore(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStoreRequest request,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Store store = storeService.updateStore(id, request, currentUser);
        return ResponseEntity.ok(StoreResponse.from(store));
    }

    private User getCurrentUser(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return userService.getUserById(userId);
    }
}
