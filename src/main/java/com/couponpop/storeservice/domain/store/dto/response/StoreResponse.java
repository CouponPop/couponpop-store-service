package com.couponpop.storeservice.domain.store.dto.response;

import com.couponpop.storeservice.domain.store.entity.Store;
import com.couponpop.couponpopcoremodule.enums.StoreCategory;

import java.time.LocalDateTime;
import java.time.LocalTime;

public record StoreResponse(
        Long id,
        Long memberId,
        String memberUsername,
        String name,
        String phone,
        String description,
        String businessNumber,
        String address,
        String dong,
        Double latitude,
        Double longitude,
        String imageUrl,
        StoreCategory storeCategory,
        LocalTime weekdayOpenTime,
        LocalTime weekdayCloseTime,
        LocalTime weekendOpenTime,
        LocalTime weekendCloseTime,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static StoreResponse from(Store store, String memberUsername) {
        return new StoreResponse(
                store.getId(),
                store.getMemberId(),
                memberUsername,
                store.getName(),
                store.getPhone(),
                store.getDescription(),
                store.getBusinessNumber(),
                store.getAddress(),
                store.getDong(),
                store.getLatitude(),
                store.getLongitude(),
                store.getImageUrl(),
                store.getStoreCategory(),
                store.getWeekdayOpenTime(),
                store.getWeekdayCloseTime(),
                store.getWeekendOpenTime(),
                store.getWeekendCloseTime(),
                store.getCreatedAt(),
                store.getUpdatedAt()
        );
    }
}

