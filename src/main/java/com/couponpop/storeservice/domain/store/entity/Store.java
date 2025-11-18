package com.couponpop.storeservice.domain.store.entity;

import com.couponpop.storeservice.common.entity.BaseEntity;
import com.couponpop.couponpopcoremodule.enums.StoreCategory;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "stores")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
public class Store extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "store_category", nullable = false)
    @Enumerated(EnumType.STRING)
    private StoreCategory storeCategory;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 30)
    private String phone;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "business_number", nullable = false, length = 30)
    private String businessNumber;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false, length = 50)
    private String dong;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(nullable = false, columnDefinition = "POINT SRID 4326", insertable = false, updatable = false)
    private Point location;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "weekday_open_time", nullable = false)
    private LocalTime weekdayOpenTime;

    @Column(name = "weekday_close_time", nullable = false)
    private LocalTime weekdayCloseTime;

    @Column(name = "weekend_open_time", nullable = false)
    private LocalTime weekendOpenTime;

    @Column(name = "weekend_close_time", nullable = false)
    private LocalTime weekendCloseTime;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private Store(Long memberId, 
                String name, 
                String phone, 
                String description, 
                String businessNumber,
                String address, 
                String dong,
                double latitude, 
                double longitude, 
                String imageUrl,
                StoreCategory storeCategory, 
                LocalTime weekdayOpenTime, 
                LocalTime weekdayCloseTime,
                LocalTime weekendOpenTime, 
                LocalTime weekendCloseTime) {
        this.memberId = memberId;
        this.name = name;
        this.phone = phone;
        this.description = description;
        this.businessNumber = businessNumber;
        this.address = address;
        this.dong = dong;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imageUrl = imageUrl;
        this.storeCategory = storeCategory;
        this.weekdayOpenTime = weekdayOpenTime;
        this.weekdayCloseTime = weekdayCloseTime;
        this.weekendOpenTime = weekendOpenTime;
        this.weekendCloseTime = weekendCloseTime;
    }

    public static Store createStore(Long memberId,
                                  String name,
                                  String phone,
                                  String description,
                                  String businessNumber,
                                  String address,
                                  String dong,
                                  double latitude,
                                  double longitude,
                                  String imageUrl,
                                  StoreCategory storeCategory,
                                  LocalTime weekdayOpenTime,
                                  LocalTime weekdayCloseTime,
                                  LocalTime weekendOpenTime,
                                  LocalTime weekendCloseTime) {

        return Store.builder()
                .memberId(memberId)
                .name(name)
                .phone(phone)
                .description(description)
                .businessNumber(businessNumber)
                .address(address)
                .dong(dong)
                .latitude(latitude)
                .longitude(longitude)
                .imageUrl(imageUrl)
                .storeCategory(storeCategory)
                .weekdayOpenTime(weekdayOpenTime)
                .weekdayCloseTime(weekdayCloseTime)
                .weekendOpenTime(weekendOpenTime)
                .weekendCloseTime(weekendCloseTime)
                .build();
    }

    public void updateStoreInfo(String name, 
                                String phone, 
                                String description, 
                                String businessNumber,
                                String address, 
                                String dong,
                                double latitude, 
                                double longitude, 
                                String imageUrl,
                                StoreCategory storeCategory, 
                                LocalTime weekdayOpenTime, 
                                LocalTime weekdayCloseTime,
                                LocalTime weekendOpenTime, 
                                LocalTime weekendCloseTime) {
        this.name = name;
        this.phone = phone;
        this.description = description;
        this.businessNumber = businessNumber;
        this.address = address;
        this.dong = dong;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imageUrl = imageUrl;
        this.storeCategory = storeCategory;
        this.weekdayOpenTime = weekdayOpenTime;
        this.weekdayCloseTime = weekdayCloseTime;
        this.weekendOpenTime = weekendOpenTime;
        this.weekendCloseTime = weekendCloseTime;
    }

    public void deleteStore() {
        this.deletedAt = LocalDateTime.now();
    }

}
