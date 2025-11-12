package com.couponpop.storeservice.domain.store.service;

import com.couponpop.storeservice.domain.store.document.StoreDocument;
import com.couponpop.storeservice.domain.store.dto.response.StoreMapResponse;
import com.couponpop.storeservice.domain.store.dto.response.StoreResponse;
import com.couponpop.storeservice.domain.store.dto.response.StoreSearchResponse;
import com.couponpop.storeservice.domain.store.dto.response.StoreSuggestResponse;
import com.couponpop.couponpopcoremodule.enums.StoreCategory;
import com.couponpop.storeservice.external.openai.service.OpenAIEmbeddingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.elasticsearch.core.query.Query;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("StoreSearchService 테스트")
class StoreSearchServiceTest {

    @Mock
    private ElasticsearchOperations elasticsearchOperations;

    @Mock
    private OpenAIEmbeddingService openAIEmbeddingService;

    @InjectMocks
    private StoreSearchService storeSearchService;

    @Test
    @DisplayName("매장명으로 검색 성공")
    void searchStoresByName_Success() {
        // given
        String keyword = "스타벅스";
        
        StoreDocument document1 = createStoreDocument(
                1L, 1L, "testuser", "스타벅스 홍대점", "02123456789",
                "홍대 스타벅스", "1234567890", "서울시 마포구", "홍대동",
                37.5665, 126.9780, "https://example.com/image.jpg",
                StoreCategory.CAFE, "09:00", "22:00", "10:00", "23:00"
        );
        
        StoreDocument document2 = createStoreDocument(
                2L, 2L, "testuser2", "스타벅스 강남점", "02987654321",
                "강남 스타벅스", "0987654321", "서울시 강남구", "역삼동",
                37.5000, 127.0000, "https://example.com/image2.jpg",
                StoreCategory.CAFE, "09:00", "22:00", "10:00", "23:00"
        );

        SearchHit<StoreDocument> hit1 = createSearchHit(document1);
        SearchHit<StoreDocument> hit2 = createSearchHit(document2);
        SearchHits<StoreDocument> searchHits = createSearchHits(Arrays.asList(hit1, hit2));

        given(elasticsearchOperations.search(any(Query.class), eq(StoreDocument.class)))
                .willReturn(searchHits);

        // when
        List<StoreResponse> result = storeSearchService.searchStoresByName(keyword);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("스타벅스 홍대점");
        assertThat(result.get(1).name()).isEqualTo("스타벅스 강남점");
    }

    @Test
    @DisplayName("매장명 검색 - 검색 결과 없음")
    void searchStoresByName_NoResults() {
        // given
        String keyword = "존재하지않는매장";
        SearchHits<StoreDocument> emptySearchHits = createSearchHits(Arrays.asList());

        given(elasticsearchOperations.search(any(Query.class), eq(StoreDocument.class)))
                .willReturn(emptySearchHits);

        // when
        List<StoreResponse> result = storeSearchService.searchStoresByName(keyword);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("매장명 검색 - 예외 발생 시 빈 리스트 반환")
    void searchStoresByName_ExceptionOccurs_ReturnsEmptyList() {
        // given
        String keyword = "스타벅스";
        given(elasticsearchOperations.search(any(Query.class), eq(StoreDocument.class)))
                .willThrow(new RuntimeException("Elasticsearch error"));

        // when
        List<StoreResponse> result = storeSearchService.searchStoresByName(keyword);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("위치 기반 매장 검색 성공")
    void searchStoresByLocation_Success() {
        // given
        double latitude = 37.5665;
        double longitude = 126.9780;
        double radius = 5.0;

        StoreDocument document1 = createStoreDocument(
                1L, 1L, "testuser", "스타벅스 홍대점", "02123456789",
                "홍대 스타벅스", "1234567890", "서울시 마포구", "홍대동",
                37.5665, 126.9780, "https://example.com/image.jpg",
                StoreCategory.CAFE, "09:00", "22:00", "10:00", "23:00"
        );

        StoreDocument document2 = createStoreDocument(
                2L, 2L, "testuser2", "카페베네", "02987654321",
                "홍대 카페베네", "0987654321", "서울시 마포구", "홍대동",
                37.5670, 126.9785, "https://example.com/image2.jpg",
                StoreCategory.CAFE, "08:00", "21:00", "09:00", "22:00"
        );

        SearchHit<StoreDocument> hit1 = createSearchHitWithDistance(document1, 0.5);
        SearchHit<StoreDocument> hit2 = createSearchHitWithDistance(document2, 1.2);
        SearchHits<StoreDocument> searchHits = createSearchHits(Arrays.asList(hit1, hit2));

        given(elasticsearchOperations.search(any(Query.class), eq(StoreDocument.class)))
                .willReturn(searchHits);

        // when
        List<StoreMapResponse> result = storeSearchService.searchStoresByLocation(latitude, longitude, radius);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("스타벅스 홍대점");
        assertThat(result.get(0).distance()).isEqualTo(0.5);
        assertThat(result.get(1).name()).isEqualTo("카페베네");
        assertThat(result.get(1).distance()).isEqualTo(1.2);
    }

    @Test
    @DisplayName("위치 기반 매장 검색 - 반경 내 매장 없음")
    void searchStoresByLocation_NoResults() {
        // given
        double latitude = 37.5665;
        double longitude = 126.9780;
        double radius = 1.0;
        SearchHits<StoreDocument> emptySearchHits = createSearchHits(Arrays.asList());

        given(elasticsearchOperations.search(any(Query.class), eq(StoreDocument.class)))
                .willReturn(emptySearchHits);

        // when
        List<StoreMapResponse> result = storeSearchService.searchStoresByLocation(latitude, longitude, radius);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("위치 기반 매장 검색 - 예외 발생 시 빈 리스트 반환")
    void searchStoresByLocation_ExceptionOccurs_ReturnsEmptyList() {
        // given
        double latitude = 37.5665;
        double longitude = 126.9780;
        double radius = 5.0;

        given(elasticsearchOperations.search(any(Query.class), eq(StoreDocument.class)))
                .willThrow(new RuntimeException("Elasticsearch error"));

        // when
        List<StoreMapResponse> result = storeSearchService.searchStoresByLocation(latitude, longitude, radius);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("위치 기반 매장 검색 - sort 값이 null일 때 Haversine 공식으로 계산")
    void searchStoresByLocation_WithoutSortValue_CalculatesDistance() {
        // given
        double latitude = 37.5665;
        double longitude = 126.9780;
        double radius = 5.0;

        StoreDocument document = createStoreDocument(
                1L, 1L, "testuser", "스타벅스 홍대점", "02123456789",
                "홍대 스타벅스", "1234567890", "서울시 마포구", "홍대동",
                37.5665, 126.9780, "https://example.com/image.jpg",
                StoreCategory.CAFE, "09:00", "22:00", "10:00", "23:00"
        );

        SearchHit<StoreDocument> hit = createSearchHitWithoutDistance(document);
        SearchHits<StoreDocument> searchHits = createSearchHits(Arrays.asList(hit));

        given(elasticsearchOperations.search(any(Query.class), eq(StoreDocument.class)))
                .willReturn(searchHits);

        // when
        List<StoreMapResponse> result = storeSearchService.searchStoresByLocation(latitude, longitude, radius);

        // then
        assertThat(result).hasSize(1);
        // 같은 좌표이므로 거리가 0에 가까움 (Haversine으로 계산된 값)
        assertThat(result.get(0).distance()).isCloseTo(0.0, offset(0.01));
    }

    @Test
    @DisplayName("위치 기반 매장 검색 - 거리가 실제로 0.0일 때 sort 값 사용")
    void searchStoresByLocation_WithZeroDistance_UsesSortValue() {
        // given
        double latitude = 37.5665;
        double longitude = 126.9780;
        double radius = 5.0;

        StoreDocument document = createStoreDocument(
                1L, 1L, "testuser", "스타벅스 홍대점", "02123456789",
                "홍대 스타벅스", "1234567890", "서울시 마포구", "홍대동",
                37.5665, 126.9780, "https://example.com/image.jpg",
                StoreCategory.CAFE, "09:00", "22:00", "10:00", "23:00"
        );

        // sort 값이 실제로 0.0인 경우
        SearchHit<StoreDocument> hit = createSearchHitWithDistance(document, 0.0);
        SearchHits<StoreDocument> searchHits = createSearchHits(Arrays.asList(hit));

        given(elasticsearchOperations.search(any(Query.class), eq(StoreDocument.class)))
                .willReturn(searchHits);

        // when
        List<StoreMapResponse> result = storeSearchService.searchStoresByLocation(latitude, longitude, radius);

        // then
        assertThat(result).hasSize(1);
        // sort 값(0.0)을 그대로 사용해야 함 (Haversine 계산 X)
        assertThat(result.get(0).distance()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("위치 기반 매장 검색 - 다양한 카테고리")
    void searchStoresByLocation_DifferentCategories() {
        // given
        double latitude = 37.5665;
        double longitude = 126.9780;
        double radius = 5.0;

        StoreDocument cafeDocument = createStoreDocument(
                1L, 1L, "testuser", "스타벅스 홍대점", "02123456789",
                "홍대 스타벅스", "1234567890", "서울시 마포구", "홍대동",
                37.5665, 126.9780, "https://example.com/image.jpg",
                StoreCategory.CAFE, "09:00", "22:00", "10:00", "23:00"
        );

        StoreDocument foodDocument = createStoreDocument(
                2L, 2L, "testuser2", "맛있는 식당", "02987654321",
                "홍대 식당", "0987654321", "서울시 마포구", "홍대동",
                37.5670, 126.9785, "https://example.com/image2.jpg",
                StoreCategory.FOOD, "11:00", "22:00", "12:00", "23:00"
        );

        SearchHit<StoreDocument> hit1 = createSearchHitWithDistance(cafeDocument, 0.5);
        SearchHit<StoreDocument> hit2 = createSearchHitWithDistance(foodDocument, 1.2);
        SearchHits<StoreDocument> searchHits = createSearchHits(Arrays.asList(hit1, hit2));

        given(elasticsearchOperations.search(any(Query.class), eq(StoreDocument.class)))
                .willReturn(searchHits);

        // when
        List<StoreMapResponse> result = storeSearchService.searchStoresByLocation(latitude, longitude, radius);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).storeCategory()).isEqualTo(StoreCategory.CAFE);
        assertThat(result.get(1).storeCategory()).isEqualTo(StoreCategory.FOOD);
    }

    private StoreDocument createStoreDocument(Long storeId, Long memberId, String memberUsername,
                                              String name, String phone, String description,
                                              String businessNumber, String address, String dong,
                                              double lat, double lon, String imageUrl,
                                              StoreCategory category, String weekdayOpen,
                                              String weekdayClose, String weekendOpen, String weekendClose) {
        StoreDocument document = mock(StoreDocument.class, RETURNS_DEEP_STUBS);
        GeoPoint location = new GeoPoint(lat, lon);
        
        // 필요한 메서드만 stub
        given(document.getStoreId()).willReturn(storeId);
        given(document.getMemberId()).willReturn(memberId);
        given(document.getMemberUsername()).willReturn(memberUsername);
        given(document.getName()).willReturn(name);
        given(document.getPhone()).willReturn(phone);
        given(document.getDescription()).willReturn(description);
        given(document.getBusinessNumber()).willReturn(businessNumber);
        given(document.getAddress()).willReturn(address);
        given(document.getDong()).willReturn(dong);
        given(document.getLocation()).willReturn(location);
        given(document.getImageUrl()).willReturn(imageUrl);
        given(document.getStoreCategory()).willReturn(category);
        given(document.getWeekdayOpenTime()).willReturn(weekdayOpen);
        given(document.getWeekdayCloseTime()).willReturn(weekdayClose);
        given(document.getWeekendOpenTime()).willReturn(weekendOpen);
        given(document.getWeekendCloseTime()).willReturn(weekendClose);
        given(document.getCreatedAt()).willReturn(LocalDateTime.now());
        given(document.getUpdatedAt()).willReturn(LocalDateTime.now());
        
        return document;
    }

    @SuppressWarnings("unchecked")
    private SearchHit<StoreDocument> createSearchHit(StoreDocument document) {
        SearchHit<StoreDocument> hit = mock(SearchHit.class);
        given(hit.getContent()).willReturn(document);
        given(hit.getSortValues()).willReturn(Arrays.asList());
        return hit;
    }

    @SuppressWarnings("unchecked")
    private SearchHit<StoreDocument> createSearchHitWithDistance(StoreDocument document, double distance) {
        SearchHit<StoreDocument> hit = mock(SearchHit.class);
        given(hit.getContent()).willReturn(document);
        given(hit.getSortValues()).willReturn(Arrays.asList(distance));
        return hit;
    }

    @SuppressWarnings("unchecked")
    private SearchHit<StoreDocument> createSearchHitWithoutDistance(StoreDocument document) {
        SearchHit<StoreDocument> hit = mock(SearchHit.class);
        given(hit.getContent()).willReturn(document);
        given(hit.getSortValues()).willReturn(Arrays.asList());
        return hit;
    }

    @SuppressWarnings("unchecked")
    private SearchHits<StoreDocument> createSearchHits(List<SearchHit<StoreDocument>> hits) {
        SearchHits<StoreDocument> searchHits = mock(SearchHits.class);
        given(searchHits.stream()).willReturn(hits.stream());
        return searchHits;
    }

    @Test
    @DisplayName("검색 추천 기능 성공")
    void searchStoresWithRecommendation_Success() {
        // given
        String keyword = "스타벅스";
        
        StoreDocument document = createStoreDocument(
                1L, 1L, "testuser", "스타벅스 홍대점", "02123456789",
                "홍대 스타벅스", "1234567890", "서울시 마포구", "홍대동",
                37.5665, 126.9780, "https://example.com/image.jpg",
                StoreCategory.CAFE, "09:00", "22:00", "10:00", "23:00"
        );

        @SuppressWarnings("unchecked")
        SearchHit<StoreDocument> hit = mock(SearchHit.class);
        given(hit.getContent()).willReturn(document);
        given(hit.getScore()).willReturn(10.5f);
        @SuppressWarnings("unchecked")
        SearchHits<StoreDocument> searchHits = mock(SearchHits.class);
        given(searchHits.stream()).willReturn(Arrays.asList(hit).stream());

        given(elasticsearchOperations.search(any(Query.class), eq(StoreDocument.class)))
                .willReturn(searchHits);

        // when
        List<StoreSearchResponse> result = storeSearchService.searchStoresWithRecommendation(keyword);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).name()).isEqualTo("스타벅스 홍대점");
    }

    @Test
    @DisplayName("검색 추천 기능 - 빈 키워드")
    void searchStoresWithRecommendation_EmptyKeyword() {
        // given
        String keyword = "";

        // when
        List<StoreSearchResponse> result = storeSearchService.searchStoresWithRecommendation(keyword);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("검색 추천 기능 - null 키워드")
    void searchStoresWithRecommendation_NullKeyword() {
        // given
        String keyword = null;

        // when
        List<StoreSearchResponse> result = storeSearchService.searchStoresWithRecommendation(keyword);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("자동완성 제안 성공")
    void suggestStores_Success() {
        // given
        String keyword = "스타";
        
        StoreDocument document = createStoreDocument(
                1L, 1L, "testuser", "스타벅스 홍대점", "02123456789",
                "홍대 스타벅스", "1234567890", "서울시 마포구", "홍대동",
                37.5665, 126.9780, "https://example.com/image.jpg",
                StoreCategory.CAFE, "09:00", "22:00", "10:00", "23:00"
        );

        SearchHit<StoreDocument> hit = createSearchHit(document);
        SearchHits<StoreDocument> searchHits = createSearchHits(Arrays.asList(hit));

        given(elasticsearchOperations.search(any(Query.class), eq(StoreDocument.class)))
                .willReturn(searchHits);

        // when
        List<StoreSuggestResponse> result = storeSearchService.suggestStores(keyword);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).name()).isEqualTo("스타벅스 홍대점");
    }

    @Test
    @DisplayName("자동완성 제안 - 빈 키워드")
    void suggestStores_EmptyKeyword() {
        // given
        String keyword = "";

        // when
        List<StoreSuggestResponse> result = storeSearchService.suggestStores(keyword);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("자동완성 제안 - null 키워드")
    void suggestStores_NullKeyword() {
        // given
        String keyword = null;

        // when
        List<StoreSuggestResponse> result = storeSearchService.suggestStores(keyword);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("하이브리드 검색 - 임베딩 생성 실패 시 키워드 검색으로 폴백")
    void executeHybridSearch_EmbeddingFailure_FallbackToKeywordSearch() {
        // given
        String keyword = "카페";
        List<StoreSearchResponse> fallbackResults = List.of(
                new StoreSearchResponse(1L, "카페 모카", "서울시 마포구", "홍대동", StoreCategory.CAFE,
                        "https://example.com/image.jpg", 37.56, 126.97, 1.0f)
        );

        StoreSearchService spyService = spy(storeSearchService);

        doReturn(null).when(openAIEmbeddingService).generateEmbedding(keyword);
        doReturn(fallbackResults).when(spyService).searchStoresWithRecommendation(keyword);

        // when
        List<StoreSearchResponse> result = spyService.executeHybridSearch(keyword);

        // then
        assertThat(result).isEqualTo(fallbackResults);
        verify(spyService, times(1)).searchStoresWithRecommendation(keyword);
        verifyNoInteractions(elasticsearchOperations);
    }

    @Test
    @DisplayName("시맨틱 검색 - 임베딩 기반 검색 성공")
    void executeSemanticSearch_Success() {
        // given
        String keyword = "디저트 카페";

        StoreDocument document = createStoreDocument(
                1L, 1L, "member", "스위트 카페", "02123456789",
                "디저트 전문 카페", "1234567890", "서울시 강남구", "역삼동",
                37.501, 127.002, "https://example.com/cafe.jpg",
                StoreCategory.CAFE, "09:00", "22:00", "10:00", "23:00"
        );

        @SuppressWarnings("unchecked")
        SearchHit<StoreDocument> hit = mock(SearchHit.class);
        given(hit.getContent()).willReturn(document);
        given(hit.getScore()).willReturn(7.5f);
        SearchHits<StoreDocument> searchHits = createSearchHits(List.of(hit));

        doReturn(List.of(0.1f, 0.2f, 0.3f)).when(openAIEmbeddingService).generateEmbedding(keyword);
        given(elasticsearchOperations.search(any(Query.class), eq(StoreDocument.class)))
                .willReturn(searchHits);

        // when
        List<StoreSearchResponse> result = storeSearchService.executeSemanticSearch(keyword);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("스위트 카페");
        assertThat(result.get(0).score()).isEqualTo(7.5f);
        verify(openAIEmbeddingService, times(1)).generateEmbedding(keyword);
        verify(elasticsearchOperations, times(1)).search(any(Query.class), eq(StoreDocument.class));
    }
}

