package com.couponpop.storeservice.domain.store.dto.response;

import java.util.List;

/**
 * 매장 검색 페이지네이션 응답 DTO
 * Keyset Pagination을 사용
 */
public record StoreSearchPageResponse(
        List<StoreSearchResponse> stores,
        Long nextCursor,  
        Integer size,     
        Boolean hasNext   
) {
    public static StoreSearchPageResponse of(List<StoreSearchResponse> stores, Integer size) {
        if (stores.isEmpty()) {
            return new StoreSearchPageResponse(stores, null, size, false);
        }
        
        Long nextCursor = stores.get(stores.size() - 1).id();
        Boolean hasNext = stores.size() >= size;
        
        return new StoreSearchPageResponse(stores, hasNext ? nextCursor : null, stores.size(), hasNext);
    }
}


