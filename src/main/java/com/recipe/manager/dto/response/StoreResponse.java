package com.recipe.manager.dto.response;

import com.recipe.manager.entity.Store;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class StoreResponse {

    private Long id;
    private String storeCode;
    private String name;
    private String location;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static StoreResponse from(Store store) {
        return StoreResponse.builder()
                .id(store.getId())
                .storeCode(store.getStoreCode())
                .name(store.getName())
                .location(store.getLocation())
                .createdAt(store.getCreatedAt())
                .updatedAt(store.getUpdatedAt())
                .build();
    }
}
