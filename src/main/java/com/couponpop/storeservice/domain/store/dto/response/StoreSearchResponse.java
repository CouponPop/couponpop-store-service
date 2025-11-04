package com.couponpop.storeservice.domain.store.dto.response;

import com.couponpop.storeservice.domain.store.document.StoreDocument;
import com.couponpop.storeservice.domain.store.enums.StoreCategory;

/**
 * 매장 검색 추천 응답 DTO
 * 검색 결과의 관련도 점수와 주요 정보만 포함
 */
public record StoreSearchResponse(
        Long id,
        String name,
        String address,
        String dong,
        StoreCategory storeCategory,
        String imageUrl,
        Double latitude,
        Double longitude,
        Float score
) {

    public static StoreSearchResponse of(StoreDocument document, Float score) {
        return new StoreSearchResponse(
                document.getStoreId(),
                document.getName(),
                document.getAddress(),
                document.getDong(),
                document.getStoreCategory(),
                document.getImageUrl(),
                document.getLocation().getLat(),
                document.getLocation().getLon(),
                score
        );
    }
}
