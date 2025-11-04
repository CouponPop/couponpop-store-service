package com.couponpop.storeservice.common.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * 현재 Store 서비스는 다른 서비스를 호출하지 않으므로 사용하지 않습니다.
 * 나중에 다른 서비스를 호출해야 할 경우 이 설정을 활성화합니다.
 */
// @Configuration
// @EnableFeignClients(basePackages = "com.couponpop.storeservice")
public class OpenFeignConfig {
    // 현재 사용하지 않음
    // 통신 방향: CouponEvent → Store (단방향)
    // Store 서비스는 다른 서비스를 호출하지 않음
}

