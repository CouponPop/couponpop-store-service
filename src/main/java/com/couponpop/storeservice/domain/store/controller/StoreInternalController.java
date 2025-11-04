package com.couponpop.storeservice.domain.store.controller;

import com.couponpop.storeservice.common.dto.couponevent.response.StoreOwnershipResponse;
import com.couponpop.storeservice.common.dto.store.request.cursor.StoreCouponEventsStatisticsCursor;
import com.couponpop.storeservice.common.dto.store.response.StoreResponse;
import com.couponpop.storeservice.common.response.ApiResponse;
import com.couponpop.storeservice.domain.store.service.StoreInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 내부 서비스 간 통신을 위한 Store 도메인 컨트롤러
 * 다른 도메인 서비스(CouponEvent 등)가 Store 도메인 데이터를 필요로 할 때 사용
 */
@RestController
@RequestMapping("/internal/v1/stores")
@RequiredArgsConstructor
public class StoreInternalController {

    private final StoreInternalService storeInternalService;

    /**
     * 매장 소유권 검증
     * 요청 도메인: CouponEvent
     */
    @GetMapping("/ownership")
    public ResponseEntity<ApiResponse<StoreOwnershipResponse>> checkOwnership(
            @RequestParam Long storeId,
            @RequestParam Long memberId) {

        StoreOwnershipResponse response = storeInternalService.checkOwnership(storeId, memberId);
        return ApiResponse.success(response);
    }

    /**
     * 회원 ID에 해당하는 cursor 기반 매장 목록 조회
     * 요청 도메인: CouponEvent
     */
    @GetMapping("/owner/{memberId}")
    public ResponseEntity<ApiResponse<List<StoreResponse>>> findStoresByOwner(
            @PathVariable Long memberId,
            @RequestParam(required = false) Long lastStoreId,
            @RequestParam(defaultValue = "10") int pageSize) {

        StoreCouponEventsStatisticsCursor cursor = StoreCouponEventsStatisticsCursor.ofNullable(lastStoreId);
        List<StoreResponse> response = storeInternalService.findStoresByOwner(memberId, cursor, pageSize);
        return ApiResponse.success(response);
    }

    /**
     * 매장 ID에 해당하는 DTO 반환
     * 요청 도메인: CouponEvent
     */
    @GetMapping("/{storeId}")
    public ResponseEntity<ApiResponse<StoreResponse>> findById(@PathVariable Long storeId) {

        StoreResponse response = storeInternalService.findByIdOrElseThrow(storeId);
        return ApiResponse.success(response);
    }

    /**
     * 여러 매장 ID에 해당하는 DTO 목록 반환
     * 요청 도메인: CouponEvent 등
     */
    @PostMapping("/query")
    public ResponseEntity<ApiResponse<List<StoreResponse>>> findAllByIds(@RequestBody List<Long> storeIds) {

        List<StoreResponse> response = storeInternalService.findAllByIds(storeIds);
        return ApiResponse.success(response);
    }
}

