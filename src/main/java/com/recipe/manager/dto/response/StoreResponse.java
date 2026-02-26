package com.recipe.manager.dto.response;

import com.recipe.manager.entity.Store;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class StoreResponse {

    private final Long id;
    private final String storeCode;
    private final String name;
    private final String location;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

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
