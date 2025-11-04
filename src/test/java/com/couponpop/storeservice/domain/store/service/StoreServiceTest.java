package com.couponpop.storeservice.domain.store.service;

import com.couponpop.storeservice.common.exception.GlobalException;
import com.couponpop.storeservice.domain.store.dto.request.CreateStoreRequest;
import com.couponpop.storeservice.domain.store.dto.response.StoreMapResponse;
import com.couponpop.storeservice.domain.store.dto.response.StoreResponse;
import com.couponpop.storeservice.domain.store.entity.Store;
import com.couponpop.storeservice.domain.store.enums.StoreCategory;
import com.couponpop.storeservice.domain.store.repository.StoreRepository;
import com.couponpop.storeservice.utils.TestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("StoreService 테스트")
class StoreServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private StoreElasticsearchSyncService elasticsearchSyncService;

    @Mock
    private StoreSearchService storeSearchService;

    @InjectMocks
    private StoreService storeService;

    @Test
    @DisplayName("매장 등록 성공")
    void createStore_Success() {

        // given
        Long memberId = 1L;
        String memberUsername = "testuser";
        CreateStoreRequest request = createStoreRequest();
        Store savedStore = createStore(memberId);

        given(storeRepository.save(any(Store.class)))
                .willReturn(savedStore);

        // when
        StoreResponse result = storeService.createStore(memberId, memberUsername, request);

        // then
        assertThat(result.id()).isEqualTo(savedStore.getId());
        assertThat(result.memberId()).isEqualTo(memberId);
        assertThat(result.memberUsername()).isEqualTo(memberUsername);
        assertThat(result.name()).isEqualTo(request.name());
        assertThat(result.phone()).isEqualTo(request.phone());
        assertThat(result.description()).isEqualTo(request.description());
        assertThat(result.businessNumber()).isEqualTo(request.businessNumber());
        assertThat(result.address()).isEqualTo(request.address());
        assertThat(result.dong()).isEqualTo(request.dong());
        assertThat(result.latitude()).isEqualTo(request.latitude());
        assertThat(result.longitude()).isEqualTo(request.longitude());
        assertThat(result.imageUrl()).isEqualTo(request.imageUrl());
        assertThat(result.storeCategory()).isEqualTo(request.storeCategory());
        assertThat(result.weekdayOpenTime()).isEqualTo(request.weekdayOpenTime());
        assertThat(result.weekdayCloseTime()).isEqualTo(request.weekdayCloseTime());
        assertThat(result.weekendOpenTime()).isEqualTo(request.weekendOpenTime());
        assertThat(result.weekendCloseTime()).isEqualTo(request.weekendCloseTime());

        then(storeRepository).should(times(1)).save(any(Store.class));
        then(elasticsearchSyncService).should(times(1)).indexStore(any(Store.class), eq(memberUsername));
    }

    @Test
    @DisplayName("매장 등록 시 정확한 데이터로 Store 엔티티 생성")
    void createStore_CreatesStoreWithCorrectData() {

        // given
        Long memberId = 2L;
        String memberUsername = "testuser2";
        CreateStoreRequest request = createStoreRequest();
        Store expectedStore = createStore(memberId);
        given(storeRepository.save(any(Store.class)))
                .willReturn(expectedStore);

        // when
        storeService.createStore(memberId, memberUsername, request);

        // then
        then(storeRepository).should(times(1)).save(any(Store.class));
        then(elasticsearchSyncService).should(times(1)).indexStore(any(Store.class), eq(memberUsername));
    }

    @Test
    @DisplayName("다른 카테고리 매장 등록 성공")
    void createStore_WithFoodCategory_Success() {

        // given
        Long memberId = 3L;
        String memberUsername = "testuser3";
        CreateStoreRequest request = createFoodStoreRequest();
        Store savedStore = createFoodStore(memberId);
        given(storeRepository.save(any(Store.class)))
                .willReturn(savedStore);

        // when
        StoreResponse result = storeService.createStore(memberId, memberUsername, request);

        // then
        assertThat(result.storeCategory()).isEqualTo(StoreCategory.FOOD);
        assertThat(result.name()).isEqualTo("맛있는 식당");
        assertThat(result.memberId()).isEqualTo(memberId);

        then(storeRepository).should(times(1)).save(any(Store.class));
        then(elasticsearchSyncService).should(times(1)).indexStore(any(Store.class), eq(memberUsername));
    }

    @Test
    @DisplayName("매장 등록 시 빈 문자열 description 처리")
    void createStore_WithEmptyDescription_Success() {

        // given
        Long memberId = 4L;
        CreateStoreRequest request = new CreateStoreRequest(
                "테스트 매장",
                "0212345678",
                "", // description이 빈 문자열
                "1234567890",
                "서울시 테스트구 테스트로 123",
                "테스트동",
                37.5665,
                126.9780,
                "https://example.com/test-image.jpg",
                StoreCategory.CAFE,
                LocalTime.of(9, 0),
                LocalTime.of(21, 0),
                LocalTime.of(10, 0),
                LocalTime.of(22, 0)
        );

        String memberUsername = "testuser4";
        Store savedStore = Store.createStore(
                memberId,
                "테스트 매장",
                "0212345678",
                "",
                "1234567890",
                "서울시 테스트구 테스트로 123",
                "테스트동",
                37.5665,
                126.9780,
                "https://example.com/test-image.jpg",
                StoreCategory.CAFE,
                LocalTime.of(9, 0),
                LocalTime.of(21, 0),
                LocalTime.of(10, 0),
                LocalTime.of(22, 0)
        );

        given(storeRepository.save(any(Store.class)))
                .willReturn(savedStore);

        // when
        StoreResponse result = storeService.createStore(memberId, memberUsername, request);

        // then
        assertThat(result.description()).isEmpty();
        assertThat(result.name()).isEqualTo("테스트 매장");

        then(storeRepository).should(times(1)).save(any(Store.class));
        then(elasticsearchSyncService).should(times(1)).indexStore(any(Store.class), eq(memberUsername));
    }

    @Test
    @DisplayName("매장 수정 성공")
    void updateStore_Success() {

        // given
        Long storeId = 1L;
        Long memberId = 1L;
        String memberUsername = "testuser";
        CreateStoreRequest request = createUpdateRequest();
        Store existingStore = createStore(memberId);

        given(storeRepository.findById(storeId))
                .willReturn(Optional.of(existingStore));
        // when
        StoreResponse result = storeService.updateStore(storeId, memberId, memberUsername, request);

        // then
        assertThat(result.name()).isEqualTo(request.name());
        assertThat(result.phone()).isEqualTo(request.phone());
        assertThat(result.storeCategory()).isEqualTo(request.storeCategory());

        then(storeRepository).should(times(1)).findById(storeId);
        then(storeRepository).should(times(0)).save(any(Store.class));
        then(elasticsearchSyncService).should(times(1)).updateStore(any(Store.class), eq(memberUsername));
    }

    @Test
    @DisplayName("존재하지 않는 매장 수정 시 예외 발생")
    void updateStore_WithNonExistentStore_ThrowsException() {

        // given
        Long storeId = 999L;
        Long memberId = 1L;
        CreateStoreRequest request = createUpdateRequest();

        given(storeRepository.findById(storeId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> storeService.updateStore(storeId, memberId, "testuser", request))
                .isInstanceOf(GlobalException.class)
                .hasMessage("매장을 찾을 수 없습니다.");

        then(storeRepository).should(times(1)).findById(storeId);
        then(storeRepository).should(times(0)).save(any(Store.class));
    }

    @Test
    @DisplayName("다른 카테고리로 매장 수정 성공")
    void updateStore_WithDifferentCategory_Success() {

        // given
        Long storeId = 1L;
        Long memberId = 1L;
        String memberUsername = "testuser";
        CreateStoreRequest request = createFoodUpdateRequest();
        Store existingStore = createStore(memberId);

        given(storeRepository.findById(storeId))
                .willReturn(Optional.of(existingStore));
        // when
        StoreResponse result = storeService.updateStore(storeId, memberId, memberUsername, request);

        // then
        assertThat(result.storeCategory()).isEqualTo(StoreCategory.FOOD);
        assertThat(result.name()).isEqualTo("맛있는 식당");

        then(storeRepository).should(times(1)).findById(storeId);
        then(storeRepository).should(times(0)).save(any(Store.class));
        then(elasticsearchSyncService).should(times(1)).updateStore(any(Store.class), eq(memberUsername));
    }

    @Test
    @DisplayName("다른 회원의 매장 수정 시 권한 없음 예외 발생")
    void updateStore_WithDifferentMember_ThrowsPermissionException() {

        // given
        Long storeId = 1L;
        Long memberId = 1L;
        Long otherMemberId = 2L;
        CreateStoreRequest request = createUpdateRequest();
        Store store = createStore(otherMemberId);

        given(storeRepository.findById(storeId))
                .willReturn(Optional.of(store));

        // when & then
        assertThatThrownBy(() -> storeService.updateStore(storeId, memberId, "testuser", request))
                .isInstanceOf(GlobalException.class)
                .hasMessage("매장 수정 권한이 없습니다.");

        then(storeRepository).should(times(1)).findById(storeId);
        then(storeRepository).should(times(0)).save(any(Store.class));
    }

    @Test
    @DisplayName("매장 삭제 성공")
    void deleteStore_Success() {

        // given
        Long storeId = 1L;
        Long memberId = 1L;
        Store existingStore = createStore(memberId);

        given(storeRepository.findByIdIncludingDeleted(storeId))
                .willReturn(Optional.of(existingStore));

        // when
        storeService.deleteStore(storeId, memberId);

        // then
        assertThat(existingStore.getDeletedAt()).isNotNull();
        then(storeRepository).should(times(1)).findByIdIncludingDeleted(storeId);
        then(elasticsearchSyncService).should(times(1)).deleteStore(storeId);
    }

    @Test
    @DisplayName("존재하지 않는 매장 삭제 시 예외 발생")
    void deleteStore_WithNonExistentStore_ThrowsException() {

        // given
        Long storeId = 999L;
        Long memberId = 1L;

        given(storeRepository.findByIdIncludingDeleted(storeId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> storeService.deleteStore(storeId, memberId))
                .isInstanceOf(GlobalException.class)
                .hasMessage("매장을 찾을 수 없습니다.");

        then(storeRepository).should(times(1)).findByIdIncludingDeleted(storeId);
    }

    @Test
    @DisplayName("다른 회원의 매장 삭제 시 권한 없음 예외 발생")
    void deleteStore_WithDifferentMember_ThrowsPermissionException() {

        // given
        Long storeId = 1L;
        Long memberId = 1L;
        Long otherMemberId = 2L;
        Store store = createStore(otherMemberId);

        given(storeRepository.findByIdIncludingDeleted(storeId))
                .willReturn(Optional.of(store));

        // when & then
        assertThatThrownBy(() -> storeService.deleteStore(storeId, memberId))
                .isInstanceOf(GlobalException.class)
                .hasMessage("매장 삭제 권한이 없습니다.");

        then(storeRepository).should(times(1)).findByIdIncludingDeleted(storeId);
    }

    @Test
    @DisplayName("이미 삭제된 매장 삭제 시 예외 발생")
    void deleteStore_WithAlreadyDeletedStore_ThrowsException() {

        // given
        Long storeId = 1L;
        Long memberId = 1L;
        Store deletedStore = createStore(memberId);
        deletedStore.deleteStore(); // 이미 삭제된 상태

        given(storeRepository.findByIdIncludingDeleted(storeId))
                .willReturn(Optional.of(deletedStore));

        // when & then
        assertThatThrownBy(() -> storeService.deleteStore(storeId, memberId))
                .isInstanceOf(GlobalException.class)
                .hasMessage("이미 삭제된 매장입니다.");

        then(storeRepository).should(times(1)).findByIdIncludingDeleted(storeId);
    }

    @Test
    @DisplayName("위치 기반 매장 조회 성공")
    void getStoresByLocation_Success() {

        // given
        double latitude = 37.5665; // 서울시청 위도
        double longitude = 126.9780; // 서울시청 경도
        double radius = 5.0; // 5km 반경

        StoreMapResponse response1 = new StoreMapResponse(
                3L,
                "가까운 카페",
                "서울시 중구 세종대로 110",
                "중림동",
                StoreCategory.CAFE,
                37.5665,
                126.9780,
                "https://example.com/near-cafe.jpg",
                0.5
        );
        StoreMapResponse response2 = new StoreMapResponse(
                4L,
                "먼 카페",
                "서울시 강남구 테헤란로 123",
                "역삼동",
                StoreCategory.CAFE,
                37.5000,
                127.0000,
                "https://example.com/far-cafe.jpg",
                3.2
        );
        List<StoreMapResponse> mockResults = Arrays.asList(response1, response2);

        given(storeSearchService.searchStoresByLocation(latitude, longitude, radius))
                .willReturn(mockResults);

        // when
        List<StoreMapResponse> result = storeService.getStoresByLocation(latitude, longitude, radius);

        // then
        assertThat(result).hasSize(2);
        
        // 첫 번째 매장 (가까운 매장)
        StoreMapResponse firstStore = result.get(0);
        assertThat(firstStore.id()).isEqualTo(3L);
        assertThat(firstStore.name()).isEqualTo("가까운 카페");
        assertThat(firstStore.storeCategory()).isEqualTo(StoreCategory.CAFE);
        assertThat(firstStore.distance()).isEqualTo(0.5);
        
        // 두 번째 매장 (먼 매장)
        StoreMapResponse secondStore = result.get(1);
        assertThat(secondStore.id()).isEqualTo(4L);
        assertThat(secondStore.name()).isEqualTo("먼 카페");
        assertThat(secondStore.storeCategory()).isEqualTo(StoreCategory.CAFE);
        assertThat(secondStore.distance()).isEqualTo(3.2);

        then(storeSearchService).should(times(1)).searchStoresByLocation(latitude, longitude, radius);
    }

    @Test
    @DisplayName("위치 기반 매장 조회 - 반경 내 매장이 없는 경우")
    void getStoresByLocation_NoStoresInRadius_ReturnsEmptyList() {

        // given
        double latitude = 37.5665;
        double longitude = 126.9780;
        double radius = 1.0; // 1km 반경 (매우 좁은 반경)

        given(storeSearchService.searchStoresByLocation(latitude, longitude, radius))
                .willReturn(Arrays.asList());

        // when
        List<StoreMapResponse> result = storeService.getStoresByLocation(latitude, longitude, radius);

        // then
        assertThat(result).isEmpty();

        then(storeSearchService).should(times(1)).searchStoresByLocation(latitude, longitude, radius);
    }

    @Test
    @DisplayName("위치 기반 매장 조회 - 다양한 카테고리 매장")
    void getStoresByLocation_DifferentCategories_Success() {

        // given
        double latitude = 37.5665;
        double longitude = 126.9780;
        double radius = 5.0;

        StoreMapResponse cafeResponse = new StoreMapResponse(
                5L,
                "스타벅스 강남점",
                "서울시 강남구 테헤란로 123",
                "역삼동",
                StoreCategory.CAFE,
                37.5000,
                127.0000,
                "https://example.com/starbucks-gangnam.jpg",
                1.2
        );
        StoreMapResponse foodResponse = new StoreMapResponse(
                2L,
                "맛있는 식당",
                "서울시 강남구 테헤란로 456",
                "역삼동",
                StoreCategory.FOOD,
                37.5665,
                126.9780,
                "https://example.com/food-store-image.jpg",
                2.8
        );
        List<StoreMapResponse> mockResults = Arrays.asList(cafeResponse, foodResponse);

        given(storeSearchService.searchStoresByLocation(latitude, longitude, radius))
                .willReturn(mockResults);

        // when
        List<StoreMapResponse> result = storeService.getStoresByLocation(latitude, longitude, radius);

        // then
        assertThat(result).hasSize(2);
        
        // 카페 매장 검증
        StoreMapResponse cafe = result.get(0);
        assertThat(cafe.storeCategory()).isEqualTo(StoreCategory.CAFE);
        assertThat(cafe.name()).isEqualTo("스타벅스 강남점");
        
        // 음식점 매장 검증
        StoreMapResponse food = result.get(1);
        assertThat(food.storeCategory()).isEqualTo(StoreCategory.FOOD);
        assertThat(food.name()).isEqualTo("맛있는 식당");

        then(storeSearchService).should(times(1)).searchStoresByLocation(latitude, longitude, radius);
    }

    @Test
    @DisplayName("위치 기반 매장 조회 - 거리순 정렬 확인")
    void getStoresByLocation_DistanceOrdering_Success() {

        // given
        double latitude = 37.5665;
        double longitude = 126.9780;
        double radius = 5.0;

        // 거리순으로 정렬된 결과 (가까운 순)
        StoreMapResponse response1 = new StoreMapResponse(
                1L,
                "가까운 매장",
                "서울시 중구",
                "중구동",
                StoreCategory.CAFE,
                37.5665,
                126.9780,
                "https://example.com/near.jpg",
                0.8
        );
        StoreMapResponse response2 = new StoreMapResponse(
                2L,
                "중간 매장",
                "서울시 용산구",
                "용산동",
                StoreCategory.CAFE,
                37.5665,
                126.9780,
                "https://example.com/middle.jpg",
                2.1
        );
        StoreMapResponse response3 = new StoreMapResponse(
                3L,
                "먼 매장",
                "서울시 강남구",
                "강남동",
                StoreCategory.CAFE,
                37.5665,
                126.9780,
                "https://example.com/far.jpg",
                4.5
        );
        List<StoreMapResponse> mockResults = Arrays.asList(response1, response2, response3);

        given(storeSearchService.searchStoresByLocation(latitude, longitude, radius))
                .willReturn(mockResults);

        // when
        List<StoreMapResponse> result = storeService.getStoresByLocation(latitude, longitude, radius);

        // then
        assertThat(result).hasSize(3);
        
        // 거리순 정렬 확인
        assertThat(result.get(0).distance()).isEqualTo(0.8);
        assertThat(result.get(1).distance()).isEqualTo(2.1);
        assertThat(result.get(2).distance()).isEqualTo(4.5);

        then(storeSearchService).should(times(1)).searchStoresByLocation(latitude, longitude, radius);
    }

    private CreateStoreRequest createStoreRequest() {
        return new CreateStoreRequest(
                "스타벅스 홍대점",
                "0212345678",
                "홍대 중심가에 위치한 스타벅스입니다.",
                "1234567890",
                "서울시 마포구 홍익로 123",
                "홍대동",
                37.5665,
                126.9780,
                "https://example.com/store-image.jpg",
                StoreCategory.CAFE,
                LocalTime.of(7, 0),
                LocalTime.of(22, 0),
                LocalTime.of(8, 0),
                LocalTime.of(23, 0)
        );
    }

    private CreateStoreRequest createFoodStoreRequest() {
        return new CreateStoreRequest(
                "맛있는 식당",
                "0312345678",
                "정말 맛있는 음식을 제공하는 식당입니다.",
                "9876543210",
                "서울시 강남구 테헤란로 456",
                "역삼동",
                37.5665,
                126.9780,
                "https://example.com/food-store-image.jpg",
                StoreCategory.FOOD,
                LocalTime.of(11, 0),
                LocalTime.of(22, 0),
                LocalTime.of(12, 0),
                LocalTime.of(23, 0)
        );
    }

    private Store createStore(Long memberId) {
        Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put("id", 1L);
        fieldValues.put("memberId", memberId);
        fieldValues.put("name", "스타벅스 홍대점");
        fieldValues.put("phone", "0212345678");
        fieldValues.put("description", "홍대 중심가에 위치한 스타벅스입니다.");
        fieldValues.put("businessNumber", "1234567890");
        fieldValues.put("address", "서울시 마포구 홍익로 123");
        fieldValues.put("dong", "홍대동");
        fieldValues.put("latitude", 37.5665);
        fieldValues.put("longitude", 126.9780);
        fieldValues.put("imageUrl", "https://example.com/store-image.jpg");
        fieldValues.put("storeCategory", StoreCategory.CAFE);
        fieldValues.put("weekdayOpenTime", LocalTime.of(7, 0));
        fieldValues.put("weekdayCloseTime", LocalTime.of(22, 0));
        fieldValues.put("weekendOpenTime", LocalTime.of(8, 0));
        fieldValues.put("weekendCloseTime", LocalTime.of(23, 0));
        
        return TestUtils.createEntity(Store.class, fieldValues);
    }

    private Store createFoodStore(Long memberId) {
        Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put("id", 2L);
        fieldValues.put("memberId", memberId);
        fieldValues.put("name", "맛있는 식당");
        fieldValues.put("phone", "0312345678");
        fieldValues.put("description", "정말 맛있는 음식을 제공하는 식당입니다.");
        fieldValues.put("businessNumber", "9876543210");
        fieldValues.put("address", "서울시 강남구 테헤란로 456");
        fieldValues.put("dong", "역삼동");
        fieldValues.put("latitude", 37.5665);
        fieldValues.put("longitude", 126.9780);
        fieldValues.put("imageUrl", "https://example.com/food-store-image.jpg");
        fieldValues.put("storeCategory", StoreCategory.FOOD);
        fieldValues.put("weekdayOpenTime", LocalTime.of(11, 0));
        fieldValues.put("weekdayCloseTime", LocalTime.of(22, 0));
        fieldValues.put("weekendOpenTime", LocalTime.of(12, 0));
        fieldValues.put("weekendCloseTime", LocalTime.of(23, 0));
        
        return TestUtils.createEntity(Store.class, fieldValues);
    }

    private CreateStoreRequest createUpdateRequest() {
        return new CreateStoreRequest(
                "스타벅스 홍대점 수정",
                "0212345679",
                "홍대 중심가에 위치한 스타벅스입니다. (수정됨)",
                "1234567891",
                "서울시 마포구 홍익로 124",
                "홍대동",
                37.5666,
                126.9781,
                "https://example.com/store-image-updated.jpg",
                StoreCategory.CAFE,
                LocalTime.of(8, 0),
                LocalTime.of(23, 0),
                LocalTime.of(9, 0),
                LocalTime.of(23, 30)
        );
    }

    private CreateStoreRequest createFoodUpdateRequest() {
        return new CreateStoreRequest(
                "맛있는 식당",
                "0312345678",
                "정말 맛있는 음식을 제공하는 식당입니다.",
                "9876543210",
                "서울시 강남구 테헤란로 456",
                "역삼동",
                37.5665,
                126.9780,
                "https://example.com/food-store-image.jpg",
                StoreCategory.FOOD,
                LocalTime.of(11, 0),
                LocalTime.of(22, 0),
                LocalTime.of(12, 0),
                LocalTime.of(23, 0)
        );
    }

    @Test
    @DisplayName("손님용 매장 상세 조회 성공")
    void getStoreDetailForCustomer_Success() {

        // given
        Long storeId = 1L;
        Long memberId = 10L;
        Store store = createStore(memberId);

        given(storeRepository.findById(storeId))
                .willReturn(Optional.of(store));

        // when
        var result = storeService.getStoreDetailForCustomer(storeId);

        // then
        assertThat(result.imageUrl()).isEqualTo(store.getImageUrl());
        assertThat(result.name()).isEqualTo(store.getName());
        assertThat(result.description()).isEqualTo(store.getDescription());
        assertThat(result.storeCategory()).isEqualTo(store.getStoreCategory());
        assertThat(result.address()).isEqualTo(store.getAddress());
        assertThat(result.dong()).isEqualTo(store.getDong());
        assertThat(result.weekdayOpenTime()).isEqualTo(store.getWeekdayOpenTime());
        assertThat(result.weekdayCloseTime()).isEqualTo(store.getWeekdayCloseTime());
        assertThat(result.weekendOpenTime()).isEqualTo(store.getWeekendOpenTime());
        assertThat(result.weekendCloseTime()).isEqualTo(store.getWeekendCloseTime());

        then(storeRepository).should(times(1)).findById(storeId);
    }

    @Test
    @DisplayName("손님용 매장 상세 조회 - 존재하지 않는 매장 예외")
    void getStoreDetailForCustomer_NotFound_ThrowsException() {

        // given
        Long storeId = 999L;

        given(storeRepository.findById(storeId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> storeService.getStoreDetailForCustomer(storeId))
                .isInstanceOf(GlobalException.class)
                .hasMessage("매장을 찾을 수 없습니다.");

        then(storeRepository).should(times(1)).findById(storeId);
    }

    @Test
    @DisplayName("매장명 검색 성공")
    void searchStoresByName_Success() {

        // given
        String keyword = "스타벅스";
        
        StoreResponse response1 = new StoreResponse(
                1L, 1L, "testuser", "스타벅스 홍대점", "0212345678",
                "홍대 스타벅스", "1234567890", "서울시 마포구", "홍대동",
                37.5665, 126.9780, "https://example.com/image.jpg",
                StoreCategory.CAFE, LocalTime.of(9, 0), LocalTime.of(22, 0),
                LocalTime.of(10, 0), LocalTime.of(23, 0),
                java.time.LocalDateTime.now(), java.time.LocalDateTime.now()
        );
        StoreResponse response2 = new StoreResponse(
                5L, 2L, "testuser2", "스타벅스 강남점", "0312345678",
                "강남 스타벅스", "9876543210", "서울시 강남구", "역삼동",
                37.5000, 127.0000, "https://example.com/image2.jpg",
                StoreCategory.CAFE, LocalTime.of(9, 0), LocalTime.of(22, 0),
                LocalTime.of(10, 0), LocalTime.of(23, 0),
                java.time.LocalDateTime.now(), java.time.LocalDateTime.now()
        );
        List<StoreResponse> mockResults = Arrays.asList(response1, response2);

        given(storeSearchService.searchStoresByName(keyword))
                .willReturn(mockResults);

        // when
        List<StoreResponse> result = storeService.searchStoresByName(keyword);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("스타벅스 홍대점");
        assertThat(result.get(1).name()).isEqualTo("스타벅스 강남점");

        then(storeSearchService).should(times(1)).searchStoresByName(keyword);
    }

    @Test
    @DisplayName("매장명 검색 - 검색 결과 없음")
    void searchStoresByName_NoResults_ReturnsEmptyList() {

        // given
        String keyword = "존재하지않는매장";

        given(storeSearchService.searchStoresByName(keyword))
                .willReturn(Arrays.asList());

        // when
        List<StoreResponse> result = storeService.searchStoresByName(keyword);

        // then
        assertThat(result).isEmpty();

        then(storeSearchService).should(times(1)).searchStoresByName(keyword);
    }

    @Test
    @DisplayName("소유자의 매장 목록 조회 성공")
    void getStoresByOwner_Success() {

        // given
        Long memberId = 1L;
        String memberUsername = "testuser";
        List<Store> stores = Arrays.asList(
                createStore(memberId),
                createFoodStore(memberId)
        );

        given(storeRepository.findByMemberIdOrderByCreatedAtDesc(memberId))
                .willReturn(stores);
        // when
        List<StoreResponse> result = storeService.getStoresByOwner(memberId, memberUsername);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("스타벅스 홍대점");
        assertThat(result.get(1).name()).isEqualTo("맛있는 식당");

        then(storeRepository).should(times(1)).findByMemberIdOrderByCreatedAtDesc(memberId);
    }

    @Test
    @DisplayName("소유자의 매장 목록 조회 - 매장이 없는 경우")
    void getStoresByOwner_NoStores_ReturnsEmptyList() {

        // given
        Long memberId = 1L;
        String memberUsername = "testuser";

        given(storeRepository.findByMemberIdOrderByCreatedAtDesc(memberId))
                .willReturn(Arrays.asList());
        // when
        List<StoreResponse> result = storeService.getStoresByOwner(memberId, memberUsername);

        // then
        assertThat(result).isEmpty();

        then(storeRepository).should(times(1)).findByMemberIdOrderByCreatedAtDesc(memberId);
    }

    @Test
    @DisplayName("소유자의 매장 상세 조회 성공")
    void getStoreDetail_Success() {

        // given
        Long storeId = 1L;
        Long memberId = 1L;
        Store store = createStore(memberId);

        given(storeRepository.findById(storeId))
                .willReturn(Optional.of(store));

        // when
        var result = storeService.getStoreDetail(storeId, memberId);

        // then
        assertThat(result.name()).isEqualTo(store.getName());
        assertThat(result.description()).isEqualTo(store.getDescription());
        assertThat(result.storeCategory()).isEqualTo(store.getStoreCategory());

        then(storeRepository).should(times(1)).findById(storeId);
    }

    @Test
    @DisplayName("소유자의 매장 상세 조회 - 존재하지 않는 매장")
    void getStoreDetail_NotFound_ThrowsException() {

        // given
        Long storeId = 999L;
        Long memberId = 1L;

        given(storeRepository.findById(storeId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> storeService.getStoreDetail(storeId, memberId))
                .isInstanceOf(GlobalException.class)
                .hasMessage("매장을 찾을 수 없습니다.");

        then(storeRepository).should(times(1)).findById(storeId);
    }

    @Test
    @DisplayName("소유자의 매장 상세 조회 - 다른 소유자의 매장 접근")
    void getStoreDetail_DifferentOwner_ThrowsException() {

        // given
        Long storeId = 1L;
        Long memberId = 1L;
        Long otherMemberId = 2L;
        Store store = createStore(otherMemberId);

        given(storeRepository.findById(storeId))
                .willReturn(Optional.of(store));

        // when & then
        assertThatThrownBy(() -> storeService.getStoreDetail(storeId, memberId))
                .isInstanceOf(GlobalException.class)
                .hasMessage("매장 접근 권한이 없습니다.");

        then(storeRepository).should(times(1)).findById(storeId);
    }

    
}
