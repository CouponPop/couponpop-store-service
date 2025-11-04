package com.couponpop.storeservice.domain.store.dto.response;

import com.couponpop.storeservice.domain.store.entity.Store;
import com.couponpop.storeservice.domain.store.enums.StoreCategory;

import java.time.LocalTime;

public record StoreDetailResponse(
        String imageUrl,
        String name,
        String description,
        StoreCategory storeCategory,
        String address,
        String dong,
        LocalTime weekdayOpenTime,
        LocalTime weekdayCloseTime,
        LocalTime weekendOpenTime,
        LocalTime weekendCloseTime
) {
    public static StoreDetailResponse from(Store store) {
        return new StoreDetailResponse(
                store.getImageUrl(),
                store.getName(),
                store.getDescription(),
                store.getStoreCategory(),
                store.getAddress(),
                store.getDong(),
                store.getWeekdayOpenTime(),
                store.getWeekdayCloseTime(),
                store.getWeekendOpenTime(),
                store.getWeekendCloseTime()
        );
    }
}


