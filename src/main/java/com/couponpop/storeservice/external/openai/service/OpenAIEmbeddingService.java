package com.couponpop.storeservice.external.openai.service;

import com.couponpop.storeservice.external.openai.dto.EmbeddingData;
import com.couponpop.storeservice.external.openai.dto.EmbeddingRequest;
import com.couponpop.storeservice.external.openai.dto.EmbeddingResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

/**
 * OpenAI Embedding API를 사용하여 텍스트를 벡터로 변환하는 서비스
 */
@Slf4j
@Service
public class OpenAIEmbeddingService {

    private static final String EMPTY_RESPONSE_LOG = "Empty response from OpenAI API";

    private final WebClient webClient;
    private final String model;

    public OpenAIEmbeddingService(
            @Value("${openai.api.key}") String apiKey,
            @Value("${openai.api.url}") String apiUrl,
            @Value("${openai.embedding.model}") String model) {
        this.model = model;
        this.webClient = WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
        
        log.info("OpenAI Embedding Service initialized with model: {}", model);
    }

    /**
     * 텍스트를 임베딩 벡터로 변환
     * 
     * @param text 변환할 텍스트
     * @return 1536차원 Float 리스트
     */
    public List<Float> generateEmbedding(String text) {
        try {
            if (text == null || text.trim().isEmpty()) {
                log.warn("Empty text provided for embedding generation");
                return Collections.emptyList();
            }

            log.debug("Generating embedding for text: {}", text.substring(0, Math.min(text.length(), 50)));

            EmbeddingRequest request = EmbeddingRequest.of(model, text.trim());

            EmbeddingResponse response = webClient.post()
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(EmbeddingResponse.class)
                    .block();

            if (response == null || response.data().isEmpty()) {
                log.error(EMPTY_RESPONSE_LOG);
                return Collections.emptyList();
            }

            List<Float> embedding = response.data().get(0).embedding();
            log.debug("Successfully generated embedding vector (dimension: {})", embedding.size());

            return embedding;

        } catch (Exception e) {
            log.error("Failed to generate embedding: text={}", text, e);
            return Collections.emptyList();
        }
    }

    /**
     * 여러 텍스트를 한 번에 임베딩 벡터로 변환 (배치 처리)
     * 
     * @param texts 변환할 텍스트 리스트
     * @return 임베딩 벡터 리스트
     */
    public List<List<Float>> generateEmbeddings(List<String> texts) {
        try {
            if (texts == null || texts.isEmpty()) {
                log.warn("Empty text list provided for embedding generation");
                return List.of();
            }

            log.debug("Generating embeddings for {} texts", texts.size());

            EmbeddingRequest request = new EmbeddingRequest(model, texts);

            EmbeddingResponse response = webClient.post()
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(EmbeddingResponse.class)
                    .block();

            if (response == null || response.data().isEmpty()) {
                log.error(EMPTY_RESPONSE_LOG);
                return List.of();
            }

            List<List<Float>> embeddings = response.data().stream()
                    .map(EmbeddingData::embedding)
                    .toList();

            log.debug("Successfully generated {} embedding vectors", embeddings.size());

            return embeddings;

        } catch (Exception e) {
            log.error("Failed to generate embeddings for {} texts", texts != null ? texts.size() : 0, e);
            return List.of();
        }
    }

    /**
     * 비동기로 텍스트를 임베딩 벡터로 변환
     * 
     * @param text 변환할 텍스트
     * @return 1536차원 Float 리스트를 반환하는 Mono
     */
    public Mono<List<Float>> generateEmbeddingAsync(String text) {
        try {
            if (text == null || text.trim().isEmpty()) {
                log.warn("Empty text provided for embedding generation");
                return Mono.just(Collections.<Float>emptyList());
            }

            log.debug("Generating embedding asynchronously for text: {}", 
                    text.substring(0, Math.min(text.length(), 50)));

            EmbeddingRequest request = EmbeddingRequest.of(model, text.trim());

            return webClient.post()
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(EmbeddingResponse.class)
                    .map(response -> {
                        if (response == null || response.data().isEmpty()) {
                            log.error(EMPTY_RESPONSE_LOG);
                            return Collections.<Float>emptyList();
                        }
                        List<Float> embedding = response.data().get(0).embedding();
                        log.debug("Successfully generated embedding vector (dimension: {})", embedding.size());
                        return embedding;
                    })
                    .onErrorResume(e -> {
                        log.error("Failed to generate embedding asynchronously: text={}", text, e);
                        return Mono.just(Collections.<Float>emptyList());
                    });

        } catch (Exception e) {
            log.error("Failed to generate embedding asynchronously: text={}", text, e);
            return Mono.just(Collections.<Float>emptyList());
        }
    }
}



