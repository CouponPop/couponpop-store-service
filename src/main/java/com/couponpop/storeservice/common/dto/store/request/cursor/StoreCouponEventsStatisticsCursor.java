package com.couponpop.storeservice.common.dto.store.request.cursor;

public record StoreCouponEventsStatisticsCursor(
        Long lastStoreId
) {
    public static StoreCouponEventsStatisticsCursor first() {
        return new StoreCouponEventsStatisticsCursor(null);
    }

    public static StoreCouponEventsStatisticsCursor ofNullable(Long lastStoreId) {
        return (lastStoreId != null) ?
                new StoreCouponEventsStatisticsCursor(lastStoreId) :
                StoreCouponEventsStatisticsCursor.first();
    }

}
