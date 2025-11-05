package com.couponpop.storeservice.domain.store.service;

import com.couponpop.couponpopcoremodule.dto.store.response.StoreRegionInfoResponse;
import com.couponpop.storeservice.common.dto.couponevent.response.StoreOwnershipResponse;
import com.couponpop.storeservice.common.dto.store.request.cursor.StoreCouponEventsStatisticsCursor;
import com.couponpop.storeservice.common.dto.store.response.StoreResponse;

import java.util.List;

public interface StoreInternalService {

    StoreOwnershipResponse checkOwnership(Long storeId, Long memberId);

    List<StoreResponse> findStoresByOwner(Long memberId, StoreCouponEventsStatisticsCursor cursor, int pageSize);

    StoreResponse findByIdOrElseThrow(Long storeId);

    List<StoreResponse> findAllByIds(List<Long> storeIds);

    List<StoreRegionInfoResponse> findRegionInfoByIds(List<Long> storeIds);
}
