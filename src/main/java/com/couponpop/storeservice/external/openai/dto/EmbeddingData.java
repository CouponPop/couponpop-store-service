package com.couponpop.storeservice.external.openai.dto;

import java.util.List;

/**
 * OpenAI Embedding API 응답의 개별 임베딩 데이터
 * 
 * @param embedding 임베딩 벡터 (1536차원 Float 리스트)
 * @param index 임베딩 데이터의 인덱스
 */
public record EmbeddingData(
        List<Float> embedding,
        int index
) {
}



