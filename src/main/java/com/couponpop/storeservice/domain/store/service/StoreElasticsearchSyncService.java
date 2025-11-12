package com.couponpop.storeservice.domain.store.service;

import com.couponpop.storeservice.domain.store.document.StoreDocument;
import com.couponpop.storeservice.domain.store.entity.Store;
import com.couponpop.storeservice.domain.store.repository.StoreSearchRepository;
import com.couponpop.storeservice.external.openai.service.OpenAIEmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 매장 정보와 Elasticsearch 간의 동기화를 담당하는 서비스
 * OpenAI Embedding을 사용한 시맨틱 검색을 지원합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StoreElasticsearchSyncService {

    private final StoreSearchRepository storeSearchRepository;
    private final OpenAIEmbeddingService openAIEmbeddingService;

    /**
     * 매장 생성 시 Elasticsearch에 문서 저장
     * 상점명과 설명을 결합하여 임베딩 벡터를 생성하고 함께 저장합니다.
     */
    public void indexStore(Store store, String memberUsername) {
        try {
            // 1. 상점 이름과 설명을 결합하여 임베딩 생성
            String combinedText = buildCombinedText(store);
            List<Float> embedding = openAIEmbeddingService.generateEmbedding(combinedText);
            
            // 2. 임베딩을 포함한 StoreDocument 생성 (한번에!)
            StoreDocument document = StoreDocument.from(store, memberUsername, embedding);
            
            // 3. Elasticsearch에 저장
            storeSearchRepository.save(document);
            
            log.info("Successfully indexed store to Elasticsearch with embedding: storeId={}, embeddingSize={}", 
                    store.getId(), embedding != null ? embedding.size() : 0);
        } catch (Exception e) {
            log.error("Failed to index store to Elasticsearch: storeId={}", store.getId(), e);
            // ES 동기화 실패가 비즈니스 로직을 막지 않도록 예외를 던지지 않음
        }
    }

    /**
     * 매장 수정 시 Elasticsearch 문서 업데이트
     * 상점 정보가 변경되면 임베딩 벡터도 재생성합니다.
     */
    public void updateStore(Store store, String memberUsername) {
        try {
            // 1. 상점 이름과 설명을 결합하여 임베딩 생성
            String combinedText = buildCombinedText(store);
            List<Float> embedding = openAIEmbeddingService.generateEmbedding(combinedText);
            
            // 2. 임베딩을 포함한 StoreDocument 생성 (한번에!)
            StoreDocument document = StoreDocument.from(store, memberUsername, embedding);
            
            // 3. Elasticsearch에 저장
            storeSearchRepository.save(document);
            
            log.info("Successfully updated store in Elasticsearch with embedding: storeId={}, embeddingSize={}", 
                    store.getId(), embedding != null ? embedding.size() : 0);
        } catch (Exception e) {
            log.error("Failed to update store in Elasticsearch: storeId={}", store.getId(), e);
        }
    }

    /**
     * 매장 데이터 배치를 한 번의 OpenAI API 호출로 인덱싱
     *
     * @param stores 배치로 처리할 매장 목록
     * @return 성공적으로 저장된 문서 수
     */
    public int indexStoresBatch(List<Store> stores) {
        if (stores == null || stores.isEmpty()) {
            log.debug("No stores provided for batch indexing");
            return 0;
        }

        try {
            List<String> combinedTexts = stores.stream()
                    .map(this::buildCombinedText)
                    .toList();

            List<List<Float>> embeddings = openAIEmbeddingService.generateEmbeddings(combinedTexts);
            int embeddingCount = embeddings != null ? embeddings.size() : 0;

            if (embeddingCount != stores.size()) {
                log.warn("Embedding count ({}) does not match store count ({}). Missing embeddings will be stored as null.",
                        embeddingCount, stores.size());
            }

            List<StoreDocument> documents = new ArrayList<>(stores.size());
            for (int i = 0; i < stores.size(); i++) {
                List<Float> embedding = (embeddings != null && embeddings.size() > i) ? embeddings.get(i) : null;
                documents.add(StoreDocument.from(stores.get(i), null, embedding));
            }

            storeSearchRepository.saveAll(documents);

            log.info("Successfully indexed store batch to Elasticsearch: batchSize={}, saved={}", 
                    stores.size(), documents.size());

            return documents.size();
        } catch (Exception e) {
            log.error("Failed to index store batch to Elasticsearch: batchSize={}", stores.size(), e);
            throw new RuntimeException("Batch indexing failed", e);
        }
    }

    /**
     * 매장 삭제 시 Elasticsearch 문서 제거
     */
    public void deleteStore(Long storeId) {
        try {
            storeSearchRepository.deleteByStoreId(storeId);
            log.info("Successfully deleted store from Elasticsearch: storeId={}", storeId);
        } catch (Exception e) {
            log.error("Failed to delete store from Elasticsearch: storeId={}", storeId, e);
        }
    }

    /**
     * 상점명과 설명을 결합하여 임베딩 생성용 텍스트 생성
     * 
     * @param store 상점 엔티티
     * @return 결합된 텍스트
     */
    private String buildCombinedText(Store store) {
        StringBuilder textBuilder = new StringBuilder();
        
        // 상점명 추가
        if (store.getName() != null && !store.getName().isEmpty()) {
            textBuilder.append(store.getName());
        }
        
        // 설명 추가 (있는 경우)
        if (store.getDescription() != null && !store.getDescription().isEmpty()) {
            if (textBuilder.length() > 0) {
                textBuilder.append(" ");
            }
            textBuilder.append(store.getDescription());
        }
        
        // 주소 추가 (검색 정확도 향상을 위해)
        if (store.getAddress() != null && !store.getAddress().isEmpty()) {
            if (textBuilder.length() > 0) {
                textBuilder.append(" ");
            }
            textBuilder.append(store.getAddress());
        }
        
        String combinedText = textBuilder.toString();
        log.debug("Combined text for embedding: {}", 
                combinedText.substring(0, Math.min(combinedText.length(), 100)));
        
        return combinedText;
    }
}

