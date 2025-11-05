package com.couponpop.storeservice.common.config;

import com.couponpop.storeservice.domain.store.repository.StoreSearchRepository;
import com.couponpop.storeservice.domain.store.service.StoreElasticsearchSyncService;
import com.couponpop.storeservice.domain.store.service.StoreSearchService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

@TestConfiguration
@Profile("test")
public class TestElasticsearchConfig {

    @Bean
    @Primary
    public ElasticsearchOperations elasticsearchOperations() {
        return Mockito.mock(ElasticsearchOperations.class);
    }

    @Bean
    @Primary
    public StoreSearchRepository storeSearchRepository() {
        return Mockito.mock(StoreSearchRepository.class);
    }

    @Bean
    @Primary
    public StoreElasticsearchSyncService storeElasticsearchSyncService() {
        return Mockito.mock(StoreElasticsearchSyncService.class);
    }

    @Bean
    @Primary
    public StoreSearchService storeSearchService() {
        return Mockito.mock(StoreSearchService.class);
    }
}
