package com.couponpop.storeservice.domain.store.service;

import com.couponpop.storeservice.domain.store.document.StoreDocument;
import com.couponpop.storeservice.domain.store.entity.Store;
import com.couponpop.storeservice.domain.store.repository.StoreRepository;
import com.couponpop.storeservice.domain.store.repository.StoreSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Elasticsearch 인덱스 초기화 및 재색인 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StoreIndexInitService {

    private final StoreRepository storeRepository;
    private final StoreSearchRepository storeSearchRepository;

    private static final int BATCH_SIZE = 100;

    /**
     * 모든 매장 데이터를 Elasticsearch에 재색인
     * 스트림 방식을 사용하여 커서 기반으로 대량 데이터를 안전하게 처리
     */
    @Transactional(readOnly = true)
    public void reindexAllStores() {
        log.info("Starting reindexing all stores to Elasticsearch using stream...");
        
        try {
            AtomicInteger totalProcessed = new AtomicInteger(0);
            AtomicInteger batchCount = new AtomicInteger(0);
            List<StoreDocument> batch = new ArrayList<>(BATCH_SIZE);

            // 스트림으로 매장 데이터 조회 및 배치 처리
            try (Stream<Store> storeStream = storeRepository.streamAll()) {
                storeStream.forEach(store -> {
                    StoreDocument document = StoreDocument.from(store);
                    batch.add(document);

                    // 배치 크기에 도달하면 저장
                    if (batch.size() >= BATCH_SIZE) {
                        saveBatch(batch, totalProcessed, batchCount);
                    }
                });

                // 남은 데이터 저장
                if (!batch.isEmpty()) {
                    saveBatch(batch, totalProcessed, batchCount);
                }
            }
            
            log.info("Successfully reindexed {} stores to Elasticsearch in {} batches", 
                    totalProcessed.get(), batchCount.get());
        } catch (Exception e) {
            log.error("Failed to reindex stores to Elasticsearch", e);
            throw new RuntimeException("Reindexing failed", e);
        }
    }

    /**
     * 배치 단위로 Elasticsearch에 저장
     */
    private void saveBatch(List<StoreDocument> batch,
                           AtomicInteger totalProcessed,
                           AtomicInteger batchCount) {
        
        int batchSize = batch.size();
        
        storeSearchRepository.saveAll(new ArrayList<>(batch));
        totalProcessed.addAndGet(batchSize);
        batchCount.incrementAndGet();

        log.debug("Processed batch {} with {} stores (Total: {})",
                batchCount.get(), batchSize, totalProcessed.get());

        batch.clear();
    }

    /**
     * Elasticsearch 인덱스 삭제 (개발/테스트 용도)
     */
    public void deleteAllStoresFromIndex() {
        log.warn("Deleting all stores from Elasticsearch index...");
        try {
            storeSearchRepository.deleteAll();
            log.info("Successfully deleted all stores from Elasticsearch index");
        } catch (Exception e) {
            log.error("Failed to delete stores from Elasticsearch index", e);
            throw new RuntimeException("Index deletion failed", e);
        }
    }

    /**
     * 전체 재색인 (기존 인덱스 삭제 후 재생성)
     */
    @Transactional(readOnly = true)
    public void fullReindex() {
        log.info("Starting full reindex (delete and recreate)...");
        deleteAllStoresFromIndex();
        reindexAllStores();
        log.info("Full reindex completed");
    }
}

