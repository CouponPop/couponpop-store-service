package com.couponpop.storeservice.common.dto.store.response;

import com.couponpop.storeservice.domain.store.entity.Store;
import com.couponpop.storeservice.domain.store.enums.StoreCategory;

public record StoreResponse(
        Long id,
        String name,
        StoreCategory storeCategory,
        double latitude,
        double longitude,
        String imageUrl
) {
    public static StoreResponse of(Long id, String name, StoreCategory storeCategory, double latitude, double longitude, String imageUrl) {
        return new StoreResponse(id, name, storeCategory, latitude, longitude, imageUrl);
    }

    public static StoreResponse from(Store store) {
        return new StoreResponse(
                store.getId(),
                store.getName(),
                store.getStoreCategory(),
                store.getLatitude(),
                store.getLongitude(),
                store.getImageUrl()
        );
    }
}
