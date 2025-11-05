package com.couponpop.storeservice.domain.store.controller;

import com.couponpop.couponpopcoremodule.dto.store.response.StoreRegionInfoResponse;
import com.couponpop.storeservice.common.response.ApiResponse;
import com.couponpop.storeservice.domain.store.service.StoreInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/*
 * TODO
 *   - 시스템 to 시스템 요청이기 때문에 화이트리스트('/internal/batch/**') 설정되어 있음
 *   - 추후 내부 서비스용 인증 토큰 도입 검토
 */
@RestController
@RequestMapping("/internal/batch")
@RequiredArgsConstructor
public class StoreInternalBatchController {

    private final StoreInternalService storeInternalService;

    /**
     * 매장 ID 리스트로 매장 지역 정보 조회
     */
    @PostMapping("/v1/stores/regions")
    public ResponseEntity<ApiResponse<List<StoreRegionInfoResponse>>> findRegionInfoByIds(@RequestBody List<Long> storeIds) {

        List<StoreRegionInfoResponse> response = storeInternalService.findRegionInfoByIds(storeIds);
        return ApiResponse.success(response);
    }
}
