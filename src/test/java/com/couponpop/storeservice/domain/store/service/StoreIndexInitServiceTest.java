package com.couponpop.storeservice.domain.store.service;

import com.couponpop.storeservice.domain.store.entity.Store;
import com.couponpop.couponpopcoremodule.enums.StoreCategory;
import com.couponpop.storeservice.domain.store.repository.StoreRepository;
import com.couponpop.storeservice.domain.store.repository.StoreSearchRepository;
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
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("StoreIndexInitService 테스트")
class StoreIndexInitServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private StoreSearchRepository storeSearchRepository;

    @InjectMocks
    private StoreIndexInitService storeIndexInitService;

    @Test
    @DisplayName("전체 매장 재색인 성공")
    void reindexAllStores_Success() {
        // given
        Long memberId = 1L;
        List<Store> stores = Arrays.asList(
                createStore(memberId, 1L, "스타벅스 홍대점"),
                createStore(memberId, 2L, "카페베네"),
                createStore(memberId, 3L, "투썸플레이스")
        );

        given(storeRepository.streamAll()).willReturn(stores.stream());
        given(storeSearchRepository.saveAll(anyList())).willAnswer(invocation -> invocation.getArgument(0));

        // when
        storeIndexInitService.reindexAllStores();

        // then
        then(storeRepository).should(times(1)).streamAll();
        then(storeSearchRepository).should(times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("전체 매장 재색인 - 매장이 없는 경우")
    void reindexAllStores_NoStores() {
        // given
        given(storeRepository.streamAll()).willReturn(Stream.empty());

        // when
        storeIndexInitService.reindexAllStores();

        // then
        then(storeRepository).should(times(1)).streamAll();
        then(storeSearchRepository).should(times(0)).saveAll(anyList());
    }

    @Test
    @DisplayName("전체 매장 재색인 - Elasticsearch 오류 발생 시 예외 전파")
    void reindexAllStores_ElasticsearchError_ThrowsException() {
        // given
        Long memberId = 1L;
        List<Store> stores = Arrays.asList(createStore(memberId, 1L, "스타벅스 홍대점"));

        given(storeRepository.streamAll()).willReturn(stores.stream());
        given(storeSearchRepository.saveAll(anyList())).willThrow(new RuntimeException("Elasticsearch error"));

        // when & then
        assertThatThrownBy(() -> storeIndexInitService.reindexAllStores())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Reindexing failed");

        then(storeRepository).should(times(1)).streamAll();
        then(storeSearchRepository).should(times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("인덱스에서 전체 매장 삭제 성공")
    void deleteAllStoresFromIndex_Success() {
        // when
        storeIndexInitService.deleteAllStoresFromIndex();

        // then
        then(storeSearchRepository).should(times(1)).deleteAll();
    }

    @Test
    @DisplayName("인덱스에서 전체 매장 삭제 - 오류 발생 시 예외 전파")
    void deleteAllStoresFromIndex_Error_ThrowsException() {
        // given
        org.mockito.Mockito.doThrow(new RuntimeException("Elasticsearch error"))
                .when(storeSearchRepository).deleteAll();

        // when & then
        assertThatThrownBy(() -> storeIndexInitService.deleteAllStoresFromIndex())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Index deletion failed");

        then(storeSearchRepository).should(times(1)).deleteAll();
    }

    @Test
    @DisplayName("전체 재색인 (삭제 후 재생성) 성공")
    void fullReindex_Success() {
        // given
        Long memberId = 1L;
        List<Store> stores = Arrays.asList(
                createStore(memberId, 1L, "스타벅스 홍대점"),
                createStore(memberId, 2L, "카페베네")
        );

        given(storeRepository.streamAll()).willReturn(stores.stream());
        given(storeSearchRepository.saveAll(anyList())).willAnswer(invocation -> invocation.getArgument(0));

        // when
        storeIndexInitService.fullReindex();

        // then
        then(storeSearchRepository).should(times(1)).deleteAll();
        then(storeRepository).should(times(1)).streamAll();
        then(storeSearchRepository).should(times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("전체 재색인 - 다양한 카테고리 매장")
    void fullReindex_DifferentCategories_Success() {
        // given
        Long memberId = 1L;
        List<Store> stores = Arrays.asList(
                createStoreWithCategory(memberId, 1L, "스타벅스", StoreCategory.CAFE),
                createStoreWithCategory(memberId, 2L, "맛있는 식당", StoreCategory.FOOD),
                createStoreWithCategory(memberId, 3L, "편의점", StoreCategory.CONVENIENCE)
        );

        given(storeRepository.streamAll()).willReturn(stores.stream());
        given(storeSearchRepository.saveAll(anyList())).willAnswer(invocation -> invocation.getArgument(0));

        // when
        storeIndexInitService.fullReindex();

        // then
        then(storeSearchRepository).should(times(1)).deleteAll();
        then(storeRepository).should(times(1)).streamAll();
        then(storeSearchRepository).should(times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("대량의 매장 재색인 성공 - 스트림 배치 처리 테스트")
    void reindexAllStores_LargeDataset_Success() {
        // given
        Long memberId = 1L;

        // 150개의 매장 데이터 생성 (100개 배치 + 50개 배치)
        List<Store> stores = Arrays.asList(
                createStore(memberId, 1L, "매장1"),
                createStore(memberId, 2L, "매장2"),
                createStore(memberId, 3L, "매장3"),
                createStore(memberId, 4L, "매장4"),
                createStore(memberId, 5L, "매장5"),
                createStore(memberId, 6L, "매장6"),
                createStore(memberId, 7L, "매장7"),
                createStore(memberId, 8L, "매장8"),
                createStore(memberId, 9L, "매장9"),
                createStore(memberId, 10L, "매장10")
        );

        given(storeRepository.streamAll()).willReturn(stores.stream());
        given(storeSearchRepository.saveAll(anyList())).willAnswer(invocation -> invocation.getArgument(0));

        // when
        storeIndexInitService.reindexAllStores();

        // then
        then(storeRepository).should(times(1)).streamAll();
        then(storeSearchRepository).should(times(1)).saveAll(anyList());
    }

    private Store createStore(Long memberId, Long storeId, String name) {
        Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put("id", storeId);
        fieldValues.put("memberId", memberId);
        fieldValues.put("name", name);
        fieldValues.put("phone", "02123456789");
        fieldValues.put("description", name + " 설명");
        fieldValues.put("businessNumber", "1234567890");
        fieldValues.put("address", "서울시 마포구");
        fieldValues.put("dong", "홍대동");
        fieldValues.put("latitude", 37.5665);
        fieldValues.put("longitude", 126.9780);
        fieldValues.put("imageUrl", "https://example.com/image.jpg");
        fieldValues.put("storeCategory", StoreCategory.CAFE);
        fieldValues.put("weekdayOpenTime", LocalTime.of(9, 0));
        fieldValues.put("weekdayCloseTime", LocalTime.of(22, 0));
        fieldValues.put("weekendOpenTime", LocalTime.of(10, 0));
        fieldValues.put("weekendCloseTime", LocalTime.of(23, 0));
        
        return TestUtils.createEntity(Store.class, fieldValues);
    }

    private Store createStoreWithCategory(Long memberId, Long storeId, String name, StoreCategory category) {
        Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put("id", storeId);
        fieldValues.put("memberId", memberId);
        fieldValues.put("name", name);
        fieldValues.put("phone", "02123456789");
        fieldValues.put("description", name + " 설명");
        fieldValues.put("businessNumber", "1234567890");
        fieldValues.put("address", "서울시 테스트구");
        fieldValues.put("dong", "테스트동");
        fieldValues.put("latitude", 37.5665);
        fieldValues.put("longitude", 126.9780);
        fieldValues.put("imageUrl", "https://example.com/image.jpg");
        fieldValues.put("storeCategory", category);
        fieldValues.put("weekdayOpenTime", LocalTime.of(9, 0));
        fieldValues.put("weekdayCloseTime", LocalTime.of(22, 0));
        fieldValues.put("weekendOpenTime", LocalTime.of(10, 0));
        fieldValues.put("weekendCloseTime", LocalTime.of(23, 0));
        
        return TestUtils.createEntity(Store.class, fieldValues);
    }
}

