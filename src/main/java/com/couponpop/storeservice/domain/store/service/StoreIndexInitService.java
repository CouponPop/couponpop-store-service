package com.couponpop.storeservice.domain.store.service;

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

    static final int REINDEX_BATCH_SIZE = 50;

    private final StoreRepository storeRepository;
    private final StoreSearchRepository storeSearchRepository;
    private final StoreElasticsearchSyncService syncService;

    /**
     * 모든 매장 데이터를 Elasticsearch에 재색인
     */
    @Transactional(readOnly = true)
    public void reindexAllStores() {
        log.info("Starting reindexing all stores to Elasticsearch with embeddings...");
        
        try {
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failCount = new AtomicInteger(0);

            // 모든 매장 데이터 조회 및 개별 처리 (embedding 생성 포함)
            try (Stream<Store> storeStream = storeRepository.streamAll()) {
                List<Store> batch = new ArrayList<>(REINDEX_BATCH_SIZE);
                storeStream.forEach(store -> {
                    batch.add(store);
                    if (batch.size() >= REINDEX_BATCH_SIZE) {
                        processBatch(List.copyOf(batch), successCount, failCount);
                        batch.clear();
                    }
                });

                if (!batch.isEmpty()) {
                    processBatch(List.copyOf(batch), successCount, failCount);
                    batch.clear();
                }
            }
            
            if (failCount.get() > 0) {
                log.error("Reindexing completed with failures: success={}, failed={}",
                        successCount.get(), failCount.get());
                throw new RuntimeException("Reindexing failed: " + failCount.get() + " store(s) failed");
            }

            log.info("Successfully reindexed stores to Elasticsearch: success={}, failed={}",
                    successCount.get(), failCount.get());
        } catch (Exception e) {
            log.error("Failed to reindex stores to Elasticsearch", e);
            throw new RuntimeException("Reindexing failed", e);
        }
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

    private void processBatch(List<Store> stores,
                              AtomicInteger successCount,
                              AtomicInteger failCount) {
        if (stores.isEmpty()) {
            return;
        }

        try {
            int processed = syncService.indexStoresBatch(stores);
            successCount.addAndGet(processed);

            int failedInBatch = stores.size() - processed;
            if (failedInBatch > 0) {
                failCount.addAndGet(failedInBatch);
                log.warn("Batch indexing completed with partial failures: processed={}, expected={}",
                        processed, stores.size());
            }

            if (successCount.get() > 0 && successCount.get() % REINDEX_BATCH_SIZE == 0) {
                log.info("Reindexed {} stores...", successCount.get());
            }
        } catch (Exception e) {
            failCount.addAndGet(stores.size());
            log.error("Failed to reindex batch of stores: batchSize={}", stores.size(), e);
        }
    }
}

