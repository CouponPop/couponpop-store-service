package com.couponpop.storeservice.domain.store.service;

import com.couponpop.storeservice.domain.store.document.StoreDocument;
import com.couponpop.storeservice.domain.store.entity.Store;
import com.couponpop.couponpopcoremodule.enums.StoreCategory;
import com.couponpop.storeservice.domain.store.repository.StoreSearchRepository;
import com.couponpop.storeservice.utils.TestUtils;
import com.couponpop.storeservice.external.openai.service.OpenAIEmbeddingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("StoreElasticsearchSyncService 테스트")
class StoreElasticsearchSyncServiceTest {

    @Mock
    private StoreSearchRepository storeSearchRepository;

    @Mock
    private OpenAIEmbeddingService openAIEmbeddingService;

    @InjectMocks
    private StoreElasticsearchSyncService elasticsearchSyncService;

    private void mockEmbeddingGeneration() {
        given(openAIEmbeddingService.generateEmbedding(anyString()))
                .willReturn(Collections.singletonList(0.1f));
    }

    @Test
    @DisplayName("매장 생성 시 Elasticsearch에 인덱싱 성공")
    void indexStore_Success() {
        // given
        Long memberId = 1L;
        String memberUsername = "testuser";
        Store store = createStore(memberId);
        mockEmbeddingGeneration();

        StoreDocument document = StoreDocument.from(store);
        given(storeSearchRepository.save(any(StoreDocument.class))).willReturn(document);

        // when
        elasticsearchSyncService.indexStore(store, memberUsername);

        // then
        then(storeSearchRepository).should(times(1)).save(any(StoreDocument.class));
    }

    @Test
    @DisplayName("매장 생성 시 Elasticsearch 인덱싱 실패해도 예외 발생하지 않음")
    void indexStore_ExceptionDoesNotPropagate() {
        // given
        Long memberId = 1L;
        String memberUsername = "testuser";
        Store store = createStore(memberId);
        mockEmbeddingGeneration();

        given(storeSearchRepository.save(any(StoreDocument.class)))
                .willThrow(new RuntimeException("Elasticsearch error"));

        // when & then
        // 예외가 발생하지 않아야 함
        elasticsearchSyncService.indexStore(store, memberUsername);

        then(storeSearchRepository).should(times(1)).save(any(StoreDocument.class));
    }

    @Test
    @DisplayName("매장 수정 시 Elasticsearch 업데이트 성공")
    void updateStore_Success() {
        // given
        Long memberId = 1L;
        String memberUsername = "testuser";
        Store store = createStore(memberId);
        mockEmbeddingGeneration();
        store.updateStoreInfo(
                "스타벅스 홍대점 (수정)",
                "02123456789",
                "수정된 설명",
                "1234567890",
                "서울시 마포구 홍익로 124",
                "홍대동",
                37.5666,
                126.9781,
                "https://example.com/updated.jpg",
                StoreCategory.CAFE,
                LocalTime.of(8, 0),
                LocalTime.of(23, 0),
                LocalTime.of(9, 0),
                LocalTime.of(23, 30)
        );

        StoreDocument document = StoreDocument.from(store);
        given(storeSearchRepository.save(any(StoreDocument.class))).willReturn(document);

        // when
        elasticsearchSyncService.updateStore(store, memberUsername);

        // then
        then(storeSearchRepository).should(times(1)).save(any(StoreDocument.class));
    }

    @Test
    @DisplayName("매장 수정 시 Elasticsearch 업데이트 실패해도 예외 발생하지 않음")
    void updateStore_ExceptionDoesNotPropagate() {
        // given
        Long memberId = 1L;
        String memberUsername = "testuser";
        Store store = createStore(memberId);
        mockEmbeddingGeneration();

        given(storeSearchRepository.save(any(StoreDocument.class)))
                .willThrow(new RuntimeException("Elasticsearch error"));

        // when & then
        // 예외가 발생하지 않아야 함
        elasticsearchSyncService.updateStore(store, memberUsername);

        then(storeSearchRepository).should(times(1)).save(any(StoreDocument.class));
    }

    @Test
    @DisplayName("매장 삭제 시 Elasticsearch에서 삭제 성공")
    void deleteStore_Success() {
        // given
        Long storeId = 1L;

        // when
        elasticsearchSyncService.deleteStore(storeId);

        // then
        then(storeSearchRepository).should(times(1)).deleteByStoreId(storeId);
    }

    @Test
    @DisplayName("매장 삭제 시 Elasticsearch 삭제 실패해도 예외 발생하지 않음")
    void deleteStore_ExceptionDoesNotPropagate() {
        // given
        Long storeId = 1L;
        doThrow(new RuntimeException("Elasticsearch error"))
                .when(storeSearchRepository).deleteByStoreId(storeId);

        // when & then
        // 예외가 발생하지 않아야 함
        elasticsearchSyncService.deleteStore(storeId);

        then(storeSearchRepository).should(times(1)).deleteByStoreId(storeId);
    }

    @Test
    @DisplayName("다양한 카테고리 매장 인덱싱 성공")
    void indexStore_DifferentCategories_Success() {
        // given
        Long memberId = 1L;
        String memberUsername = "testuser";
        mockEmbeddingGeneration();
        
        Store cafeStore = createStoreWithCategory(memberId, StoreCategory.CAFE);
        Store foodStore = createStoreWithCategory(memberId, StoreCategory.FOOD);
        Store convenienceStore = createStoreWithCategory(memberId, StoreCategory.CONVENIENCE);

        given(storeSearchRepository.save(any(StoreDocument.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        elasticsearchSyncService.indexStore(cafeStore, memberUsername);
        elasticsearchSyncService.indexStore(foodStore, memberUsername);
        elasticsearchSyncService.indexStore(convenienceStore, memberUsername);

        // then
        then(storeSearchRepository).should(times(3)).save(any(StoreDocument.class));
    }

    @Test
    @DisplayName("매장 배치 인덱싱 성공")
    void indexStoresBatch_Success() {
        // given
        Long memberId = 1L;
        List<Store> stores = List.of(
                createStore(memberId, 1L, "매장1"),
                createStore(memberId, 2L, "매장2")
        );

        given(openAIEmbeddingService.generateEmbeddings(anyList()))
                .willReturn(List.of(
                        Collections.singletonList(0.1f),
                        Collections.singletonList(0.2f)
                ));
        given(storeSearchRepository.saveAll(anyList())).willAnswer(invocation -> invocation.getArgument(0));

        // when
        int processed = elasticsearchSyncService.indexStoresBatch(stores);

        // then
        assertThat(processed).isEqualTo(stores.size());
        then(openAIEmbeddingService).should(times(1)).generateEmbeddings(anyList());
        then(storeSearchRepository).should(times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("배치 인덱싱 - 처리할 매장이 없으면 바로 종료")
    void indexStoresBatch_EmptyList_ReturnsZero() {
        // when
        int processed = elasticsearchSyncService.indexStoresBatch(Collections.emptyList());

        // then
        assertThat(processed).isZero();
        then(openAIEmbeddingService).shouldHaveNoInteractions();
        then(storeSearchRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("배치 인덱싱 - 임베딩 개수가 부족해도 null로 저장")
    void indexStoresBatch_EmbeddingsMismatch_SavesWithNullEmbedding() {
        // given
        Long memberId = 1L;
        List<Store> stores = List.of(
                createStore(memberId, 1L, "매장1"),
                createStore(memberId, 2L, "매장2")
        );

        given(openAIEmbeddingService.generateEmbeddings(anyList()))
                .willReturn(List.of(Collections.singletonList(0.1f)));

        given(storeSearchRepository.saveAll(anyList())).willAnswer(invocation -> invocation.getArgument(0));

        // when
        int processed = elasticsearchSyncService.indexStoresBatch(stores);

        // then
        assertThat(processed).isEqualTo(stores.size());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<StoreDocument>> captor = ArgumentCaptor.forClass(Iterable.class);
        then(storeSearchRepository).should().saveAll(captor.capture());

        List<StoreDocument> savedDocuments = StreamSupport.stream(captor.getValue().spliterator(), false)
                .toList();

        assertThat(savedDocuments).hasSize(stores.size());
        assertThat(savedDocuments.get(0).getEmbedding()).isEqualTo(Collections.singletonList(0.1f));
        assertThat(savedDocuments.get(1).getEmbedding()).isNull();
    }

    @Test
    @DisplayName("배치 인덱싱 - 저장 실패 시 예외 전파")
    void indexStoresBatch_SaveAllThrows_ExceptionPropagates() {
        // given
        Long memberId = 1L;
        List<Store> stores = List.of(
                createStore(memberId, 1L, "매장1"),
                createStore(memberId, 2L, "매장2")
        );

        given(openAIEmbeddingService.generateEmbeddings(anyList()))
                .willReturn(List.of(
                        Collections.singletonList(0.1f),
                        Collections.singletonList(0.2f)
                ));
        given(storeSearchRepository.saveAll(anyList()))
                .willThrow(new RuntimeException("Elasticsearch error"));

        // when & then
        assertThatThrownBy(() -> elasticsearchSyncService.indexStoresBatch(stores))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Batch indexing failed");
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
        fieldValues.put("imageUrl", "https://example.com/store-image.jpg");
        fieldValues.put("storeCategory", StoreCategory.CAFE);
        fieldValues.put("weekdayOpenTime", LocalTime.of(7, 0));
        fieldValues.put("weekdayCloseTime", LocalTime.of(22, 0));
        fieldValues.put("weekendOpenTime", LocalTime.of(8, 0));
        fieldValues.put("weekendCloseTime", LocalTime.of(23, 0));

        return TestUtils.createEntity(Store.class, fieldValues);
    }

    private Store createStore(Long memberId) {
        Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put("id", 1L);
        fieldValues.put("memberId", memberId);
        fieldValues.put("name", "스타벅스 홍대점");
        fieldValues.put("phone", "02123456789");
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

    private Store createStoreWithCategory(Long memberId, StoreCategory category) {
        Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put("id", 1L);
        fieldValues.put("memberId", memberId);
        fieldValues.put("name", "테스트 매장");
        fieldValues.put("phone", "02123456789");
        fieldValues.put("description", "테스트 매장입니다.");
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

