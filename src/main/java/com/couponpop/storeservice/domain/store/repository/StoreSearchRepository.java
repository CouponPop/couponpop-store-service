package com.couponpop.storeservice.domain.store.repository;

import com.couponpop.storeservice.domain.store.document.StoreDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreSearchRepository extends ElasticsearchRepository<StoreDocument, String> {
    
    /**
     * storeId로 문서 삭제
     */
    void deleteByStoreId(Long storeId);
}

