package com.couponpop.storeservice.common.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

import java.net.URI;

@Configuration
public class ElasticsearchConfig {

    @Value("${spring.data.elasticsearch.uris}")
    private String elasticsearchUri;

    /**
     * Low Level REST Client - REST API를 직접 사용
     * 예: Request request = new Request("GET", "/index/_search");
     */
    @Bean
    public RestClient restClient() {
        URI uri = URI.create(elasticsearchUri);
        HttpHost host = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
        
        return RestClient.builder(host).build();
    }

    /**
     * High Level Client - 타입 세이프한 클라이언트
     * 예: esClient.search(s -> s.index("index").query(q -> q.match(m -> m.field("field").query("value"))))
     */
    @Bean
    public ElasticsearchClient elasticsearchClient(RestClient restClient) {
        ObjectMapper objectMapper = createObjectMapper();

        ElasticsearchTransport transport = new RestClientTransport(
                restClient,
                new JacksonJsonpMapper(objectMapper)
        );

        return new ElasticsearchClient(transport);
    }

    /**
     * Spring Data Elasticsearch Operations - Repository 및 Template에서 사용
     */
    @Bean(name = {"elasticsearchOperations", "elasticsearchTemplate"})
    public ElasticsearchOperations elasticsearchOperations(ElasticsearchClient elasticsearchClient) {
        return new ElasticsearchTemplate(elasticsearchClient);
    }

    private ObjectMapper createObjectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule()) // Java 8 날짜 및 시간 API 지원
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE) // 변수명은 카멜케이스, 매핑되는 JSON은 스네이크케이스
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) // 타임스탬프 비활성화
                .disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS); // 타임스탬프 나노초 단위 비활성화
    }
}

