package com.couponpop.storeservice.domain.store.repository;

import com.couponpop.storeservice.domain.store.entity.Store;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface StoreRepository extends JpaRepository<Store, Long> {

    /**
     * 삭제된 매장을 포함하여 ID로 매장을 조회합니다.
     * 매장 삭제 시 권한 검증을 위해 사용됩니다.
     *
     * @SQLRestriction을 무시하고 모든 매장을 조회합니다.
     */
    @Query(value = "SELECT * FROM stores WHERE id = :storeId", nativeQuery = true)
    Optional<Store> findByIdIncludingDeleted(@Param("storeId") Long storeId);

    /**
     * 회원 ID로 매장 목록을 조회합니다.
     * 최신 생성 순으로 정렬하여 반환합니다.
     */
    List<Store> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    /**
     * 회원 ID로 매장 목록을 조회합니다 (ID 내림차순).
     * cursor 기반 페이징의 첫 페이지 조회에 사용.
     */
    List<Store> findByMemberIdOrderByIdDesc(Long memberId, Pageable pageable);

    /**
     * 회원 ID로 매장 목록을 조회합니다 (ID 내림차순, 지정된 ID보다 작은 값).
     * cursor 기반 페이징의 다음 페이지 조회에 사용.
     */
    List<Store> findByMemberIdAndIdLessThanOrderByIdDesc(Long memberId, Long lastStoreId, Pageable pageable);

    /**
     * 모든 매장을 스트림으로 조회합니다.
     * 대용량 데이터 처리를 위해 커서 기반으로 동작합니다.
     */
    @Query("SELECT s FROM Store s")
    @QueryHints(value = {
            @QueryHint(name = "org.hibernate.fetchSize", value = "100"),
            @QueryHint(name = "org.hibernate.readOnly", value = "true")
    })
    Stream<Store> streamAll();

    @Query("SELECT s FROM Store s WHERE s.dong IN :dongs")
    List<Store> findByDongIn(List<String> dongs);
}