package com.couponpop.storeservice.domain.store.service;

import co.elastic.clients.elasticsearch._types.DistanceUnit;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreMode;
import co.elastic.clients.json.JsonData;
import com.couponpop.storeservice.domain.store.document.StoreDocument;
import com.couponpop.storeservice.domain.store.dto.response.StoreMapResponse;
import com.couponpop.storeservice.domain.store.dto.response.StoreResponse;
import com.couponpop.storeservice.domain.store.dto.response.StoreSearchResponse;
import com.couponpop.storeservice.domain.store.dto.response.StoreSuggestResponse;
import com.couponpop.storeservice.external.openai.service.OpenAIEmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Elasticsearch를 사용한 매장 검색 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StoreSearchService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final OpenAIEmbeddingService openAIEmbeddingService;

    /**
     * 매장명으로 검색 (name 필드만 검색)
     * Fuzzy 검색: 최대 2자까지 오타 허용, 첫 1자는 정확히 일치해야 함
     */
    public List<StoreResponse> searchStoresByName(String keyword) {
        try {
            Query query = NativeQuery.builder()
                    .withQuery(q -> q
                            .match(m -> m
                                    .field("name")
                                    .query(keyword)
                                    .fuzziness("AUTO")
                                    .prefixLength(1)  // 첫 1자는 정확히 일치
                                    .maxExpansions(50)  // 성능 최적화
                            )
                    )
                    .build();

            SearchHits<StoreDocument> searchHits = elasticsearchOperations.search(query, StoreDocument.class);

            return searchHits.stream()
                    .map(SearchHit::getContent)
                    .map(this::toStoreResponse)
                    .toList();
        } catch (Exception e) {
            log.error("Failed to search stores by name: keyword={}", keyword, e);
            return List.of();
        }
    }

    /**
     * 검색 추천 기능
     * 매장명(name) 필드만 검색하며 자동완성과 관련도 점수 기반 정렬을 제공
     * 
     * 검색 전략:
     * 1. 정확한 매칭 (exact match) - 가장 높은 점수
     * 2. 자동완성 (autocomplete) - prefix 매칭
     * 3. 한국어 ngram - 부분 매칭
     * 4. 기본 한국어 분석기 - 형태소 분석
     * 5. Fuzzy 검색 - 오타 허용
     */
    public List<StoreSearchResponse> searchStoresWithRecommendation(String keyword) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return List.of();
            }

            String trimmedKeyword = keyword.trim();

            Query query = NativeQuery.builder()
                    .withQuery(q -> q
                            .bool(b -> b
                                    // should 쿼리: 점수를 누적하여 관련도 계산
                                    .should(s -> s
                                            // 1. 정확한 매칭 (가장 높은 점수)
                                            .term(t -> t
                                                    .field("name.keyword")
                                                    .value(trimmedKeyword)
                                                    .boost(10.0f)
                                            )
                                    )
                                    .should(s -> s
                                            // 2. 자동완성 매칭 (prefix)
                                            .match(m -> m
                                                    .field("name.autocomplete")
                                                    .query(trimmedKeyword)
                                                    .boost(5.0f)
                                            )
                                    )
                                    .should(s -> s
                                            // 3. 한국어 ngram 매칭
                                            .match(m -> m
                                                    .field("name.ngram")
                                                    .query(trimmedKeyword)
                                                    .boost(3.0f)
                                            )
                                    )
                                    .should(s -> s
                                            // 4. 기본 한국어 분석기 매칭
                                            .match(m -> m
                                                    .field("name")
                                                    .query(trimmedKeyword)
                                                    .boost(2.0f)
                                            )
                                    )
                                    .should(s -> s
                                            // 5. Fuzzy 검색 (오타 허용)
                                            .match(m -> m
                                                    .field("name")
                                                    .query(trimmedKeyword)
                                                    .fuzziness("AUTO")
                                                    .prefixLength(1)
                                                    .boost(1.0f)
                                            )
                                    )
                                    // 최소 1개 이상의 조건이 매칭되어야 함
                                    .minimumShouldMatch("1")
                            )
                    )
                    // 점수 기반 정렬
                    .withSort(s -> s.score(sc -> sc.order(SortOrder.Desc)))
                    // 최대 20개 결과 반환
                    .withMaxResults(20)
                    .build();

            SearchHits<StoreDocument> searchHits = elasticsearchOperations.search(query, StoreDocument.class);

            return searchHits.stream()
                    .map(hit -> StoreSearchResponse.of(
                            hit.getContent(),
                            hit.getScore()
                    ))
                    .toList();
        } catch (Exception e) {
            log.error("Failed to search stores with recommendation: keyword={}", keyword, e);
            return List.of();
        }
    }

    /**
     * 자동완성 제안
     * 검색창에서 실시간으로 매장명을 제안 (간략한 정보만 반환)
     * 
     * 검색 전략:
     * - Prefix 기반 자동완성 매칭 (autocomplete 필드)
     * - 최대 10개 제안
     */
    public List<StoreSuggestResponse> suggestStores(String keyword) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return List.of();
            }

            String trimmedKeyword = keyword.trim();

            // Prefix 기반 자동완성 쿼리
            Query query = NativeQuery.builder()
                    .withQuery(q -> q
                            .match(m -> m
                                    .field("name.autocomplete")
                                    .query(trimmedKeyword)
                            )
                    )
                    .withMaxResults(10)
                    .build();

            SearchHits<StoreDocument> searchHits = elasticsearchOperations.search(query, StoreDocument.class);

            return searchHits.stream()
                    .map(hit -> StoreSuggestResponse.from(hit.getContent()))
                    .distinct()
                    .toList();

        } catch (Exception e) {
            log.error("Failed to suggest stores: keyword={}", keyword, e);
            return List.of();
        }
    }

    /**
     * 위치 기반 매장 검색 (반경 내 매장)
     */
    public List<StoreMapResponse> searchStoresByLocation(double latitude, double longitude, double radiusKm) {
        try {
            Query query = NativeQuery.builder()
                    .withQuery(q -> q
                            .geoDistance(g -> g
                                    .field("location")
                                    .distance(radiusKm + "km")
                                    .location(l -> l
                                            .latlon(lat -> lat
                                                    .lat(latitude)
                                                    .lon(longitude)
                                            )
                                    )
                            )
                    )
                    .withSort(s -> s
                            .geoDistance(g -> g
                                    .field("location")
                                    .location(l -> l
                                            .latlon(lat -> lat
                                                    .lat(latitude)
                                                    .lon(longitude)
                                            )
                                    )
                                    .unit(DistanceUnit.Kilometers)
                            )
                    )
                    .build();

            SearchHits<StoreDocument> searchHits = elasticsearchOperations.search(query, StoreDocument.class);

            return searchHits.stream()
                    .map(hit -> toStoreMapResponseWithDistance(hit, latitude, longitude))
                    .toList();
        } catch (Exception e) {
            log.error("Failed to search stores by location: lat={}, lon={}, radius={}km", 
                    latitude, longitude, radiusKm, e);
            return List.of();
        }
    }

    private StoreResponse toStoreResponse(StoreDocument document) {
        return new StoreResponse(
                document.getStoreId(),
                document.getMemberId(),
                document.getMemberUsername(),
                document.getName(),
                document.getPhone(),
                document.getDescription(),
                document.getBusinessNumber(),
                document.getAddress(),
                document.getDong(),
                document.getLocation().getLat(),
                document.getLocation().getLon(),
                document.getImageUrl(),
                document.getStoreCategory(),
                parseLocalTime(document.getWeekdayOpenTime()),
                parseLocalTime(document.getWeekdayCloseTime()),
                parseLocalTime(document.getWeekendOpenTime()),
                parseLocalTime(document.getWeekendCloseTime()),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }
    
    private java.time.LocalTime parseLocalTime(String timeString) {
        if (timeString == null || timeString.isEmpty()) {
            return null;
        }
        try {
            return java.time.LocalTime.parse(timeString);
        } catch (Exception e) {
            log.warn("Failed to parse LocalTime: {}", timeString, e);
            return null;
        }
    }

    private StoreMapResponse toStoreMapResponseWithDistance(SearchHit<StoreDocument> hit, 
                                                             double userLat, double userLon) {
        StoreDocument document = hit.getContent();
        
        // Elasticsearch의 sort 값에서 거리 추출 (km)
        Double distance = null;
        if (!hit.getSortValues().isEmpty()) {
            Object sortValue = hit.getSortValues().get(0);
            if (sortValue instanceof Number) {
                distance = ((Number) sortValue).doubleValue();
            }
        }
        
        // sort 값이 없으면 Haversine 공식으로 직접 계산
        if (distance == null) {
            distance = calculateDistance(userLat, userLon, 
                    document.getLocation().getLat(), 
                    document.getLocation().getLon());
        }
        
        return new StoreMapResponse(
                document.getStoreId(),
                document.getName(),
                document.getAddress(),
                document.getDong(),
                document.getStoreCategory(),
                document.getLocation().getLat(),
                document.getLocation().getLon(),
                document.getImageUrl(),
                Math.round(distance * 100.0) / 100.0 // 소수점 둘째 자리까지
        );
    }
    
    /**
     * Haversine 공식을 사용한 거리 계산 (km)
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371; // 지구 반지름 (km)
        
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS * c;
    }

    /**
     * 하이브리드 검색 (BM25 + KNN with Function Score) - Basic License 호환
     * 
     * BM25 키워드 검색과 시맨틱 벡터 검색을 Function Score 쿼리로 결합하여
     * 더 정확하고 의미론적인 검색 결과를 제공합니다.
     * 
     * Basic License에서도 작동하며, Script Score를 사용하여 벡터 유사도를 계산합니다.
     * 
     * @param keyword 검색 키워드
     * @return 하이브리드 검색 결과 (점수 포함)
     */
    public List<StoreSearchResponse> executeHybridSearch(String keyword) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return List.of();
            }

            String trimmedKeyword = keyword.trim();
            log.info("Executing hybrid search (Function Score) for keyword: {}", trimmedKeyword);

            // 1. 검색어를 임베딩 벡터로 변환
            List<Float> queryEmbedding = openAIEmbeddingService.generateEmbedding(trimmedKeyword);
            
            if (queryEmbedding == null || queryEmbedding.isEmpty()) {
                log.warn("Failed to generate embedding for keyword: {}, falling back to BM25 only", trimmedKeyword);
                return searchStoresWithRecommendation(trimmedKeyword);
            }

            // 2. 하이브리드 쿼리 구성 (BM25 + KNN using Function Score)
            Query query = NativeQuery.builder()
                    .withQuery(q -> q
                            .functionScore(fs -> fs
                                    // a. BM25 쿼리 (Keyword Search)
                                    .query(qq -> qq
                                            .bool(b -> b
                                                    .should(sh -> sh
                                                            .match(m -> m
                                                                    .field("name")
                                                                    .query(trimmedKeyword)
                                                                    .boost(3.0f)
                                                            )
                                                    )
                                                    .should(sh -> sh
                                                            .match(m -> m
                                                                    .field("name.ngram")
                                                                    .query(trimmedKeyword)
                                                                    .boost(2.0f)
                                                            )
                                                    )
                                                    .should(sh -> sh
                                                            .match(m -> m
                                                                    .field("description")
                                                                    .query(trimmedKeyword)
                                                                    .boost(1.0f)
                                                            )
                                                    )
                                                    .should(sh -> sh
                                                            .match(m -> m
                                                                    .field("address")
                                                                    .query(trimmedKeyword)
                                                                    .boost(1.5f)
                                                            )
                                                    )
                                                    .minimumShouldMatch("1")
                                            )
                                    )
                                    // b. 벡터 검색 (Semantic Search)를 Score Function으로 추가
                                    .functions(fn -> fn
                                            .scriptScore(ss -> ss
                                                    .script(s -> s
                                                            // 코사인 유사도 계산 (+1.0은 점수를 양수로 만듦)
                                                            .source("cosineSimilarity(params.queryVector, 'embedding') + 1.0")
                                                            .params("queryVector", JsonData.of(queryEmbedding))
                                                    )
                                            )
                                            // 벡터 유사도 점수에 가중치를 부여하여 BM25 점수와 합산
                                            .weight(5.0)
                                    )
                                    // BM25 점수와 벡터 점수를 합산
                                    .scoreMode(FunctionScoreMode.Sum)
                            )
                    )
                    .withMaxResults(20)
                    .build();

            // 3. 검색 실행 (ElasticsearchOperations 사용)
            SearchHits<StoreDocument> searchHits = elasticsearchOperations.search(query, StoreDocument.class);

            log.info("Hybrid search (Function Score) completed: keyword={}, totalHits={}", 
                    trimmedKeyword, searchHits.getTotalHits());

            // 4. 결과 변환 및 반환
            return searchHits.stream()
                    .map(hit -> {
                        float score = 0.0f;
                        try {
                            score = hit.getScore();
                        } catch (Exception e) {
                            log.debug("Failed to get score for hit", e);
                        }
                        return StoreSearchResponse.of(hit.getContent(), score);
                    })
                    .toList();

        } catch (Exception e) {
            log.error("Failed to execute hybrid search (Function Score): keyword={}", keyword, e);
            // 에러 발생 시 기존 BM25 검색으로 폴백
            log.info("Falling back to BM25 search due to error");
            return searchStoresWithRecommendation(keyword);
        }
    }

    /**
     * 순수 시맨틱 검색 (Script Score 사용) - Basic License 호환
     * 
     * 벡터 유사도만을 사용한 순수 시맨틱 검색입니다.
     * 의미적으로 유사한 매장을 찾을 때 유용합니다.
     * 
     * @param keyword 검색 키워드
     * @return 시맨틱 검색 결과 (점수 포함)
     */
    public List<StoreSearchResponse> executeSemanticSearch(String keyword) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return List.of();
            }

            String trimmedKeyword = keyword.trim();
            log.info("Executing semantic search (Script Score) for keyword: {}", trimmedKeyword);

            // 1. 검색어를 임베딩 벡터로 변환
            List<Float> queryEmbedding = openAIEmbeddingService.generateEmbedding(trimmedKeyword);
            
            if (queryEmbedding == null || queryEmbedding.isEmpty()) {
                log.warn("Failed to generate embedding for keyword: {}", trimmedKeyword);
                return List.of();
            }

            // 2. Script Score 쿼리 구성 (순수 시맨틱 검색)
            Query query = NativeQuery.builder()
                    .withQuery(q -> q
                            .scriptScore(ss -> ss
                                    // 모든 문서를 대상으로 검색
                                    .query(qq -> qq.matchAll(ma -> ma))
                                    .script(s -> s
                                            // 코사인 유사도 계산
                                            .source("cosineSimilarity(params.queryVector, 'embedding') + 1.0")
                                            .params("queryVector", JsonData.of(queryEmbedding))
                                    )
                            )
                    )
                    .withMaxResults(20)
                    .build();

            // 3. 시맨틱 검색 실행
            SearchHits<StoreDocument> searchHits = elasticsearchOperations.search(query, StoreDocument.class);

            log.info("Semantic search (Script Score) completed: keyword={}, totalHits={}", 
                    trimmedKeyword, searchHits.getTotalHits());

            // 4. 결과 변환 및 반환
            return searchHits.stream()
                    .map(hit -> {
                        float score = 0.0f;
                        try {
                            score = hit.getScore();
                        } catch (Exception e) {
                            log.debug("Failed to get score for hit", e);
                        }
                        return StoreSearchResponse.of(hit.getContent(), score);
                    })
                    .toList();

        } catch (Exception e) {
            log.error("Failed to execute semantic search (Script Score): keyword={}", keyword, e);
            return List.of();
        }
    }
}
