package com.couponpop.storeservice.domain.store.service;

import com.couponpop.couponpopcoremodule.dto.couponevent.response.StoreOwnershipResponse;
import com.couponpop.couponpopcoremodule.dto.store.request.cursor.StoreCouponEventsStatisticsCursor;
import com.couponpop.couponpopcoremodule.dto.store.response.StoreIdsByDongResponse;
import com.couponpop.couponpopcoremodule.dto.store.response.StoreRegionInfoResponse;
import com.couponpop.couponpopcoremodule.dto.store.response.StoreResponse;
import com.couponpop.storeservice.common.exception.GlobalException;
import com.couponpop.storeservice.domain.store.entity.Store;
import com.couponpop.storeservice.domain.store.exception.StoreErrorCode;
import com.couponpop.storeservice.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 내부 서비스 간 통신을 위한 Store 도메인 서비스 구현체
 * 다른 도메인 서비스(CouponEvent 등)가 Store 도메인 데이터를 필요로 할 때 사용
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreInternalServiceImpl implements StoreInternalService {

    private final StoreRepository storeRepository;

    /**
     * 매장 소유권 검증
     * 요청 도메인: CouponEvent
     */
    @Override
    public StoreOwnershipResponse checkOwnership(Long storeId, Long memberId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new GlobalException(StoreErrorCode.STORE_NOT_FOUND));

        boolean isOwner = store.getMemberId().equals(memberId);
        return StoreOwnershipResponse.from(isOwner);
    }

    /**
     * 회원 ID에 해당하는 cursor 기반 매장 목록 조회(매장 ID 내림차순)
     * 요청 도메인: CouponEvent
     */
    @Override
    public List<StoreResponse> findStoresByOwner(Long memberId, StoreCouponEventsStatisticsCursor cursor, int pageSize) {
        Pageable pageable = PageRequest.of(0, pageSize, Sort.by(Sort.Direction.DESC, "id"));

        List<Store> stores;
        if (cursor.lastStoreId() == null) {
            // 첫 페이지
            stores = storeRepository.findByMemberIdOrderByIdDesc(memberId, pageable);
        } else {
            // 다음 페이지 (cursor 다음부터)
            stores = storeRepository.findByMemberIdAndIdLessThanOrderByIdDesc(memberId, cursor.lastStoreId(), pageable);
        }

        return stores.stream()
                .map(this::toStoreResponse)
                .toList();
    }

    /**
     * 매장 ID에 해당하는 DTO 반환
     * 필요한 값: id, name, storeCategory, latitude, longitude, imageUrl
     * 요청 도메인: CouponEvent
     */
    @Override
    public StoreResponse findByIdOrElseThrow(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new GlobalException(StoreErrorCode.STORE_NOT_FOUND));

        return toStoreResponse(store);
    }

    /**
     * 여러 매장 ID에 해당하는 DTO 목록 반환
     * 요청 도메인: CouponEvent 등
     */
    @Override
    public List<StoreResponse> findAllByIds(List<Long> storeIds) {
        List<Store> stores = storeRepository.findAllById(storeIds);

        return stores.stream()
                .map(this::toStoreResponse)
                .toList();
    }

    @Override
    public List<StoreRegionInfoResponse> findRegionInfoByIds(List<Long> storeIds) {
        List<Store> stores = storeRepository.findAllById(storeIds);

        return stores.stream()
                .map(store -> StoreRegionInfoResponse.of(
                        store.getId(),
                        store.getDong()
                ))
                .toList();
    }

    /**
     * Store entity를 core-module의 StoreResponse로 변환
     */
    private StoreResponse toStoreResponse(Store store) {
        return StoreResponse.of(
                store.getId(),
                store.getName(),
                store.getStoreCategory(),
                store.getLatitude(),
                store.getLongitude(),
                store.getImageUrl()
        );
    }

    @Override
    public List<StoreIdsByDongResponse> findStoreIdsByDongs(List<String> dongs) {
        List<Store> stores = storeRepository.findByDongIn(dongs);

        return dongs.stream()
                .map(dong -> {
                    List<Long> storeIds = stores.stream()
                            .filter(store -> store.getDong().equals(dong))
                            .map(Store::getId)
                            .toList();
                    return StoreIdsByDongResponse.of(dong, storeIds);
                })
                .toList();
    }
}
