package com.couponpop.storeservice.domain.store.dto.response;

import java.util.List;

/**
 * 매장 검색 페이지네이션 응답 DTO
 * Keyset Pagination을 사용하여 대용량 데이터를 효율적으로 처리합니다.
 */
public record StoreSearchPageResponse(
        List<StoreSearchResponse> stores,
        Long nextCursor,  // 다음 페이지를 가져오기 위한 커서 (마지막 storeId)
        Integer size,     // 현재 페이지 크기
        Boolean hasNext   // 다음 페이지 존재 여부
) {
    public static StoreSearchPageResponse of(List<StoreSearchResponse> stores, Integer size) {
        if (stores.isEmpty()) {
            return new StoreSearchPageResponse(stores, null, size, false);
        }
        
        // 마지막 항목의 storeId를 다음 커서로 사용
        Long nextCursor = stores.get(stores.size() - 1).id();
        // 현재 페이지 크기가 요청한 크기와 같으면 다음 페이지가 있을 가능성이 있음
        // 정확한 hasNext는 실제로 다음 페이지를 조회해봐야 알 수 있지만,
        // 성능을 위해 현재 페이지 크기로 판단
        Boolean hasNext = stores.size() >= size;
        
        return new StoreSearchPageResponse(stores, hasNext ? nextCursor : null, stores.size(), hasNext);
    }
}


