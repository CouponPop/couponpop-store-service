package com.couponpop.storeservice.domain.store.document;

import com.couponpop.storeservice.domain.store.entity.Store;
import com.couponpop.couponpopcoremodule.enums.StoreCategory;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import java.time.LocalDateTime;
import java.util.List;

@Document(indexName = "stores")
@Setting(
    refreshInterval = "1s",
    replicas = 1,
    shards = 3,
    settingPath = "/elasticsearch/store-analyzer-settings.json"
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreDocument {

    @Id
    private String id;

    @Field(type = FieldType.Long, name = "store_id")
    private Long storeId;

    @Field(type = FieldType.Long, name = "member_id")
    private Long memberId;

    @Field(type = FieldType.Keyword, name = "member_username")
    private String memberUsername;

    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "korean_analyzer"),
        otherFields = {
            @InnerField(suffix = "autocomplete", type = FieldType.Text, 
                        analyzer = "autocomplete_analyzer", searchAnalyzer = "autocomplete_search_analyzer"),
            @InnerField(suffix = "ngram", type = FieldType.Text, 
                        analyzer = "korean_ngram_analyzer"),
            @InnerField(suffix = "keyword", type = FieldType.Keyword)
        }
    )
    private String name;

    @Field(type = FieldType.Keyword)
    private String phone;

    @Field(type = FieldType.Text, analyzer = "korean_analyzer")
    private String description;

    @Field(type = FieldType.Keyword, name = "business_number")
    private String businessNumber;

    @Field(type = FieldType.Text, analyzer = "korean_analyzer")
    private String address;

    @Field(type = FieldType.Keyword)
    private String dong;

    @GeoPointField
    private GeoPoint location;

    @Field(type = FieldType.Keyword, name = "image_url")
    private String imageUrl;

    @Field(type = FieldType.Keyword, name = "store_category")
    private StoreCategory storeCategory;

    @Field(type = FieldType.Text, name = "weekday_open_time")
    private String weekdayOpenTime;

    @Field(type = FieldType.Text, name = "weekday_close_time")
    private String weekdayCloseTime;

    @Field(type = FieldType.Text, name = "weekend_open_time")
    private String weekendOpenTime;

    @Field(type = FieldType.Text, name = "weekend_close_time")
    private String weekendCloseTime;

    @Field(type = FieldType.Date, name = "created_at", format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date, name = "updated_at", format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime updatedAt;

    /**
     * OpenAI 임베딩 벡터 (1536 차원)
     * 시맨틱 검색(Semantic Search)에 사용됨
     */
    @Field(type = FieldType.Dense_Vector, dims = 1536)
    private List<Float> embedding;

    @Builder
    private StoreDocument(String id,
                          Long storeId,
                          Long memberId,
                          String memberUsername,
                          String name,
                          String phone,
                          String description,
                          String businessNumber,
                          String address,
                          String dong,
                          GeoPoint location,
                          String imageUrl,
                          StoreCategory storeCategory,
                          String weekdayOpenTime,
                          String weekdayCloseTime,
                          String weekendOpenTime,
                          String weekendCloseTime,
                          LocalDateTime createdAt,
                          LocalDateTime updatedAt,
                          List<Float> embedding) {
        this.id = id;
        this.storeId = storeId;
        this.memberId = memberId;
        this.memberUsername = memberUsername;
        this.name = name;
        this.phone = phone;
        this.description = description;
        this.businessNumber = businessNumber;
        this.address = address;
        this.dong = dong;
        this.location = location;
        this.imageUrl = imageUrl;
        this.storeCategory = storeCategory;
        this.weekdayOpenTime = weekdayOpenTime;
        this.weekdayCloseTime = weekdayCloseTime;
        this.weekendOpenTime = weekendOpenTime;
        this.weekendCloseTime = weekendCloseTime;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.embedding = embedding;
    }

    public static StoreDocument from(Store store) {
        return from(store, null, null);
    }

    public static StoreDocument from(Store store, String memberUsername) {
        return from(store, memberUsername, null);
    }

    /**
     * Store 엔티티로부터 StoreDocument 생성 (embedding 포함)
     * 
     * @param store Store 엔티티
     * @param memberUsername 회원 사용자명 (선택)
     * @param embedding OpenAI 임베딩 벡터 (선택, 1536차원)
     * @return StoreDocument
     */
    public static StoreDocument from(Store store, String memberUsername, List<Float> embedding) {
        return StoreDocument.builder()
                .id(String.valueOf(store.getId())) // Elasticsearch 문서 ID를 storeId로 설정하여 업데이트 시 덮어쓰기 가능
                .storeId(store.getId())
                .memberId(store.getMemberId())
                .memberUsername(memberUsername)
                .name(store.getName())
                .phone(store.getPhone())
                .description(store.getDescription())
                .businessNumber(store.getBusinessNumber())
                .address(store.getAddress())
                .dong(store.getDong())
                .location(new GeoPoint(store.getLatitude(), store.getLongitude()))
                .imageUrl(store.getImageUrl())
                .storeCategory(store.getStoreCategory())
                .weekdayOpenTime(store.getWeekdayOpenTime() != null ? store.getWeekdayOpenTime().toString() : null)
                .weekdayCloseTime(store.getWeekdayCloseTime() != null ? store.getWeekdayCloseTime().toString() : null)
                .weekendOpenTime(store.getWeekendOpenTime() != null ? store.getWeekendOpenTime().toString() : null)
                .weekendCloseTime(store.getWeekendCloseTime() != null ? store.getWeekendCloseTime().toString() : null)
                .createdAt(store.getCreatedAt())
                .updatedAt(store.getUpdatedAt())
                .embedding(embedding)  // 임베딩 벡터 추가
                .build();
    }
}

