package com.couponpop.storeservice.external.openai.dto;

import java.util.List;

/**
 * OpenAI Embedding API 요청 DTO
 * 
 * @param model 사용할 임베딩 모델 (예: "text-embedding-3-small")
 * @param input 임베딩을 생성할 텍스트 리스트
 */
public record EmbeddingRequest(
        String model,
        List<String> input
) {
    /**
     * 단일 텍스트로 요청 생성
     */
    public static EmbeddingRequest of(String model, String text) {
        return new EmbeddingRequest(model, List.of(text));
    }
}



