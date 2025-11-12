package com.couponpop.storeservice.domain.store.service;

import com.couponpop.storeservice.domain.store.entity.Store;
import com.couponpop.storeservice.domain.store.repository.StoreRepository;
import com.couponpop.storeservice.domain.store.repository.StoreSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                storeStream.forEach(store -> {
                    try {
                        // StoreElasticsearchSyncService를 통해 embedding 포함하여 인덱싱
                        syncService.indexStore(store, null);
                        successCount.incrementAndGet();
                        
                        if (successCount.get() % 10 == 0) {
                            log.info("Reindexed {} stores...", successCount.get());
                        }
                    } catch (Exception e) {
                        failCount.incrementAndGet();
                        log.error("Failed to reindex store: storeId={}", store.getId(), e);
                    }
                });
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

    /**
     * 애플리케이션 시작 시 자동 재인덱싱 (개발 환경용)
     * 프로덕션에서는 이 메서드를 제거하거나 비활성화하세요
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("Application ready - checking if reindexing is needed...");
        
        try {
            // Elasticsearch에 문서가 있는지 확인
            long documentCount = storeSearchRepository.count();
            long dbCount = storeRepository.count();
            
            log.info("Elasticsearch documents: {}, Database records: {}", documentCount, dbCount);
            
            // 문서가 없거나 개수가 다르면 재인덱싱
            if (documentCount == 0 || documentCount != dbCount) {
                log.warn("Document count mismatch detected. Starting automatic reindexing...");
                reindexAllStores();
            } else {
                // 첫 번째 문서에 embedding이 있는지 확인
                log.info("Document count matches. Checking if embeddings exist...");
                // embedding이 없으면 재인덱싱 필요 (이 부분은 선택적)
            }
        } catch (Exception e) {
            log.error("Failed to check or reindex on startup", e);
        }
    }
}

