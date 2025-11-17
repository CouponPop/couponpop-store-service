package com.couponpop.storeservice.domain.store.controller;

import com.couponpop.storeservice.common.response.ApiResponse;
import com.couponpop.security.annotation.CurrentMember;
import com.couponpop.security.dto.AuthMember;
import com.couponpop.storeservice.domain.store.dto.request.CreateStoreRequest;
import com.couponpop.storeservice.domain.store.dto.response.StoreDetailResponse;
import com.couponpop.storeservice.domain.store.dto.response.StoreMapResponse;
import com.couponpop.storeservice.domain.store.dto.response.StoreResponse;
import com.couponpop.storeservice.domain.store.dto.response.StoreSearchPageResponse;
import com.couponpop.storeservice.domain.store.dto.response.StoreSuggestResponse;
import com.couponpop.storeservice.domain.store.service.StoreSearchService;
import com.couponpop.storeservice.domain.store.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;
    private final StoreSearchService storeSearchService;

    @GetMapping("/owner/stores")
    public ResponseEntity<ApiResponse<List<StoreResponse>>> getStores(@CurrentMember AuthMember authMember) {

        List<StoreResponse> storeResponses = storeService.getStoresByOwner(authMember.id(), authMember.username());

        return ApiResponse.success(storeResponses);
    }

    @PostMapping("/owner/stores")
    public ResponseEntity<ApiResponse<StoreResponse>> createStore(@CurrentMember AuthMember authMember, @RequestBody @Valid CreateStoreRequest request) {

        StoreResponse storeResponse = storeService.createStore(authMember.id(), authMember.username(), request);

        return ApiResponse.created(storeResponse);
    }

    @PutMapping("/owner/stores/{storeId}")
    public ResponseEntity<ApiResponse<StoreResponse>> updateStore(@CurrentMember AuthMember authMember, @PathVariable Long storeId, @RequestBody @Valid CreateStoreRequest request) {

        StoreResponse storeResponse = storeService.updateStore(storeId, authMember.id(), authMember.username(), request);

        return ApiResponse.success(storeResponse);
    }

    @DeleteMapping("/owner/stores/{storeId}")
    public ResponseEntity<ApiResponse<Void>> deleteStore(@CurrentMember AuthMember authMember, @PathVariable Long storeId) {

        storeService.deleteStore(storeId, authMember.id());

        return ApiResponse.noContent();
    }

    @GetMapping("/owner/stores/{storeId}")
    public ResponseEntity<ApiResponse<StoreDetailResponse>> getStoreDetail(@CurrentMember AuthMember authMember, @PathVariable Long storeId) {

        StoreDetailResponse response = storeService.getStoreDetail(storeId, authMember.id());

        return ApiResponse.success(response);
    }

    @GetMapping("/stores")
    public ResponseEntity<ApiResponse<List<StoreMapResponse>>> getStoresByLocation(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "5.0") double radius) {

        List<StoreMapResponse> stores = storeService.getStoresByLocation(lat, lng, radius);

        return ApiResponse.success(stores);
    }

    @GetMapping("/stores/{storeId}")
    public ResponseEntity<ApiResponse<StoreDetailResponse>> getStoreDetailForCustomer(@PathVariable Long storeId) {

        StoreDetailResponse response = storeService.getStoreDetailForCustomer(storeId);

        return ApiResponse.success(response);
    }

    /**
     * 하이브리드 검색 (BM25 + 벡터 검색) - Keyset Pagination 지원
     * 키워드 검색과 의미론적 검색을 결합하여 더 정확한 결과를 제공합니다.
     * 
     * @param keyword 검색 키워드
     * @param cursor 이전 페이지의 마지막 storeId (선택, 첫 페이지는 생략)
     * @param size 페이지 크기 (기본값: 20, 최대: 100)
     */
    @GetMapping("/stores/search")
    public ResponseEntity<ApiResponse<StoreSearchPageResponse>> searchStores(
            @RequestParam String keyword,
            @RequestParam(required = false) Long cursor,
            @RequestParam(required = false, defaultValue = "20") Integer size) {

        StoreSearchPageResponse response = storeSearchService.executeHybridSearch(keyword, cursor, size);

        return ApiResponse.success(response);
    }

    /**
     * 순수 시맨틱 검색 (벡터 검색만 사용) - Keyset Pagination 지원
     * 의미적으로 유사한 매장을 찾을 때 유용합니다.
     * 
     * @param keyword 검색 키워드
     * @param cursor 이전 페이지의 마지막 storeId (선택, 첫 페이지는 생략)
     * @param size 페이지 크기 (기본값: 20, 최대: 100)
     */
    @GetMapping("/stores/search/semantic")
    public ResponseEntity<ApiResponse<StoreSearchPageResponse>> searchStoresSemantic(
            @RequestParam String keyword,
            @RequestParam(required = false) Long cursor,
            @RequestParam(required = false, defaultValue = "20") Integer size) {

        StoreSearchPageResponse response = storeSearchService.executeSemanticSearch(keyword, cursor, size);

        return ApiResponse.success(response);
    }

    /**
     * BM25 검색 (키워드 검색만 사용) - Keyset Pagination 지원
     * 기존 검색 방식으로, 정확한 키워드 매칭에 특화되어 있습니다.
     * 
     * @param keyword 검색 키워드
     * @param cursor 이전 페이지의 마지막 storeId (선택, 첫 페이지는 생략)
     * @param size 페이지 크기 (기본값: 20, 최대: 100)
     */
    @GetMapping("/stores/search/keyword")
    public ResponseEntity<ApiResponse<StoreSearchPageResponse>> searchStoresKeyword(
            @RequestParam String keyword,
            @RequestParam(required = false) Long cursor,
            @RequestParam(required = false, defaultValue = "20") Integer size) {

        StoreSearchPageResponse response = storeSearchService.searchStoresWithRecommendation(keyword, cursor, size);

        return ApiResponse.success(response);
    }

    @GetMapping("/stores/search/suggest")
    public ResponseEntity<ApiResponse<List<StoreSuggestResponse>>> suggestStores(@RequestParam String keyword) {

        List<StoreSuggestResponse> suggestions = storeSearchService.suggestStores(keyword);

        return ApiResponse.success(suggestions);
    }
}
