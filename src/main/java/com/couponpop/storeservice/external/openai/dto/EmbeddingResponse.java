package com.couponpop.storeservice.external.openai.dto;

import java.util.List;

/**
 * OpenAI Embedding API 응답 DTO
 * 
 * @param data 임베딩 데이터 리스트
 * @param model 사용된 모델명
 * @param usage API 사용량 정보
 */
public record EmbeddingResponse(
        List<EmbeddingData> data,
        String model,
        Usage usage
) {
    /**
     * API 사용량 정보
     */
    public record Usage(
            int prompt_tokens,
            int total_tokens
    ) {
    }
}



