package com.couponpop.storeservice.domain.store.dto.request;

import com.couponpop.storeservice.domain.store.enums.StoreCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

public record CreateStoreRequest(
        @NotBlank(message = "매장명은 필수입니다")
        @Size(max = 255, message = "매장명은 255자를 초과할 수 없습니다")
        String name,

        @NotBlank(message = "전화번호는 필수입니다")
        @Pattern(regexp = "^\\d{11}$", message = "전화번호는 11자리 숫자여야 합니다")
        String phone,

        @NotBlank(message = "매장 설명은 필수입니다")
        @Size(max = 500, message = "매장 설명은 500자를 초과할 수 없습니다")
        String description,

        @NotBlank(message = "사업자 번호는 필수입니다")
        @Pattern(regexp = "^\\d{10}$", message = "사업자 번호는 10자리 숫자여야 합니다")
        String businessNumber,

        @NotBlank(message = "주소는 필수입니다")
        @Size(max = 255, message = "주소는 255자를 초과할 수 없습니다")
        String address,

        @NotBlank(message = "동은 필수입니다")
        @Size(max = 50, message = "동은 50자를 초과할 수 없습니다")
        String dong,

        @NotNull(message = "위도는 필수입니다")
        Double latitude,

        @NotNull(message = "경도는 필수입니다")
        Double longitude,

        @Size(max = 500, message = "이미지 URL은 500자를 초과할 수 없습니다")
        String imageUrl,

        @NotNull(message = "매장 카테고리는 필수입니다")
        StoreCategory storeCategory,

        @NotNull(message = "평일 오픈 시간은 필수입니다")
        LocalTime weekdayOpenTime,

        @NotNull(message = "평일 마감 시간은 필수입니다")
        LocalTime weekdayCloseTime,

        @NotNull(message = "주말 오픈 시간은 필수입니다")
        LocalTime weekendOpenTime,

        @NotNull(message = "주말 마감 시간은 필수입니다")
        LocalTime weekendCloseTime
) {
}

