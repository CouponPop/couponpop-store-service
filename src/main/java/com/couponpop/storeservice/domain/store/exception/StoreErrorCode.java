package com.couponpop.storeservice.domain.store.exception;

import com.couponpop.storeservice.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum StoreErrorCode implements ErrorCode {

    STORE_NOT_FOUND(HttpStatus.BAD_REQUEST, "매장을 찾을 수 없습니다."),
    STORE_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 삭제된 매장입니다."),
    STORE_ACCESS_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "매장 접근 권한이 없습니다."),
    STORE_UPDATE_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "매장 수정 권한이 없습니다."),
    STORE_DELETE_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "매장 삭제 권한이 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
