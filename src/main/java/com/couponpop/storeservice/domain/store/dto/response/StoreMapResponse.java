package com.couponpop.storeservice.domain.store.dto.response;

import com.couponpop.storeservice.domain.store.entity.Store;
import com.couponpop.couponpopcoremodule.enums.StoreCategory;

public record StoreMapResponse(
        Long id,
        String name,
        String address,
        String dong,
        StoreCategory storeCategory,
        double latitude,
        double longitude,
        String imageUrl,
        double distance // km 단위
) {
    public static StoreMapResponse from(Store store, double distance) {
        return new StoreMapResponse(
                store.getId(),
                store.getName(),
                store.getAddress(),
                store.getDong(),
                store.getStoreCategory(),
                store.getLatitude(),
                store.getLongitude(),
                store.getImageUrl(),
                distance
        );
    }
}
