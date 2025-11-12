package com.couponpop.storeservice.domain.store.controller;

import com.couponpop.security.annotation.CurrentMember;
import com.couponpop.security.dto.AuthMember;
import com.couponpop.storeservice.common.exception.GlobalException;
import com.couponpop.storeservice.common.response.ApiResponse;
import com.couponpop.storeservice.domain.store.exception.StoreErrorCode;
import com.couponpop.storeservice.domain.store.service.StoreIndexInitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 관리자 전용 Store 인덱스 관리 컨트롤러
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/stores/index")
public class StoreIndexAdminController {

    private final StoreIndexInitService storeIndexInitService;

    /**
     * 전체 매장 데이터를 Elasticsearch에 재색인합니다.
     */
    @PostMapping("/reindex")
    public ResponseEntity<ApiResponse<String>> reindexAllStores(@CurrentMember AuthMember authMember) {

        validateAdminRole(authMember);

        log.info("Admin request: Starting reindexing all stores... (Admin: {})", authMember.username());

        try {
            storeIndexInitService.reindexAllStores();
            return ApiResponse.success("Reindexing initiated successfully. Check logs for progress.");
        } catch (Exception e) {
            log.error("Reindexing failed by admin endpoint", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Elasticsearch 인덱스 전체 삭제 후 전체 재색인을 수행합니다.
     */
    @PostMapping("/full-reindex")
    public ResponseEntity<ApiResponse<String>> fullReindex(@CurrentMember AuthMember authMember) {

        validateAdminRole(authMember);

        log.warn("Admin request: Starting FULL REINDEX (Delete and Recreate)... (Admin: {})", authMember.username());

        try {
            storeIndexInitService.fullReindex();
            return ApiResponse.success("Full Reindexing initiated successfully. Check logs for progress.");
        } catch (Exception e) {
            log.error("Full Reindexing failed via admin endpoint", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Elasticsearch 인덱스의 모든 데이터를 삭제합니다.
     */
    @PostMapping("/delete-all")
    public ResponseEntity<ApiResponse<String>> deleteAllFromIndex(@CurrentMember AuthMember authMember) {

        validateAdminRole(authMember);

        log.warn("Admin request: Deleting all stores from Elasticsearch index... (Admin: {})", authMember.username());

        try {
            storeIndexInitService.deleteAllStoresFromIndex();
            return ApiResponse.success("All stores deleted from Elasticsearch index successfully.");
        } catch (Exception e) {
            log.error("Delete operation failed via admin endpoint", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private void validateAdminRole(AuthMember authMember) {

        if (!"ROLE_ADMIN".equals(authMember.role())) {
            throw new GlobalException(StoreErrorCode.ADMIN_PERMISSION_REQUIRED);
        }
    }
}

