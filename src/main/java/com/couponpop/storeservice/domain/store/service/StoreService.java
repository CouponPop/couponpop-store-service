package com.couponpop.storeservice.domain.store.service;

import com.couponpop.storeservice.common.exception.GlobalException;
import com.couponpop.storeservice.domain.store.dto.request.CreateStoreRequest;
import com.couponpop.storeservice.domain.store.dto.response.StoreDetailResponse;
import com.couponpop.storeservice.domain.store.dto.response.StoreMapResponse;
import com.couponpop.storeservice.domain.store.dto.response.StoreResponse;
import com.couponpop.storeservice.domain.store.entity.Store;
import com.couponpop.storeservice.domain.store.exception.StoreErrorCode;
import com.couponpop.storeservice.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final StoreElasticsearchSyncService elasticsearchSyncService;
    private final StoreSearchService storeSearchService;

    @Transactional
    public StoreResponse createStore(Long memberId, String memberUsername, CreateStoreRequest request) {

        Store store = Store.createStore(
                memberId,
                request.name(),
                request.phone(),
                request.description(),
                request.businessNumber(),
                request.address(),
                request.dong(),
                request.latitude(),
                request.longitude(),
                request.imageUrl(),
                request.storeCategory(),
                request.weekdayOpenTime(),
                request.weekdayCloseTime(),
                request.weekendOpenTime(),
                request.weekendCloseTime()
        );

        Store savedStore = storeRepository.save(store);
        
        // Elasticsearch에 동기화
        elasticsearchSyncService.indexStore(savedStore, memberUsername);

        return StoreResponse.from(savedStore, memberUsername);
    }

    @Transactional
    public StoreResponse updateStore(Long storeId, Long memberId, String memberUsername, CreateStoreRequest request) {

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new GlobalException(StoreErrorCode.STORE_NOT_FOUND));

        // 매장 소유자 검증
        if (!store.getMemberId().equals(memberId)) {
            throw new GlobalException(StoreErrorCode.STORE_UPDATE_PERMISSION_DENIED);
        }

        store.updateStoreInfo(
                request.name(),
                request.phone(),
                request.description(),
                request.businessNumber(),
                request.address(),
                request.dong(),
                request.latitude(),
                request.longitude(),
                request.imageUrl(),
                request.storeCategory(),
                request.weekdayOpenTime(),
                request.weekdayCloseTime(),
                request.weekendOpenTime(),
                request.weekendCloseTime()
        );
        
        // Elasticsearch에 동기화
        elasticsearchSyncService.updateStore(store, memberUsername);

        return StoreResponse.from(store, memberUsername);
    }

    @Transactional(readOnly = true)
    public List<StoreResponse> getStoresByOwner(Long memberId, String memberUsername) {

        List<Store> stores = storeRepository.findByMemberIdOrderByCreatedAtDesc(memberId);

        return stores.stream()
                .map(store -> StoreResponse.from(store, memberUsername))
                .toList();
    }

    @Transactional
    public void deleteStore(Long storeId, Long memberId) {

        Store store = storeRepository.findByIdIncludingDeleted(storeId)
                .orElseThrow(() -> new GlobalException(StoreErrorCode.STORE_NOT_FOUND));

        // 이미 삭제된 매장인지 확인
        if (store.getDeletedAt() != null) {
            throw new GlobalException(StoreErrorCode.STORE_ALREADY_DELETED);
        }

        // 매장 소유자 검증
        if (!store.getMemberId().equals(memberId)) {
            throw new GlobalException(StoreErrorCode.STORE_DELETE_PERMISSION_DENIED);
        }

        store.deleteStore();
        
        // Elasticsearch에서 삭제
        elasticsearchSyncService.deleteStore(storeId);
    }

    @Transactional(readOnly = true)
    public StoreDetailResponse getStoreDetail(Long storeId, Long memberId) {

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new GlobalException(StoreErrorCode.STORE_NOT_FOUND));

        if (!store.getMemberId().equals(memberId)) {
            throw new GlobalException(StoreErrorCode.STORE_ACCESS_PERMISSION_DENIED);
        }

        return StoreDetailResponse.from(store);
    }

    @Transactional(readOnly = true)
    public StoreDetailResponse getStoreDetailForCustomer(Long storeId) {

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new GlobalException(StoreErrorCode.STORE_NOT_FOUND));

        return StoreDetailResponse.from(store);
    }

    @Transactional(readOnly = true)
    public List<StoreResponse> searchStoresByName(String keyword) {
        // Elasticsearch를 사용한 검색으로 변경
        return storeSearchService.searchStoresByName(keyword);
    }

    @Transactional(readOnly = true)
    public List<StoreMapResponse> getStoresByLocation(double latitude, double longitude, double radiusKm) {
        // Elasticsearch를 사용한 위치 기반 검색으로 변경
        return storeSearchService.searchStoresByLocation(latitude, longitude, radiusKm);
    }
}
