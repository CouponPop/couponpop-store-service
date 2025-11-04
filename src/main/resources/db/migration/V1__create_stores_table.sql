-- 매장 테이블 생성
CREATE TABLE stores (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL COMMENT '소유자 회원 ID',
    store_category VARCHAR(20) NOT NULL COMMENT '매장 카테고리 (CAFE, FOOD, CONVENIENCE)',
    name VARCHAR(255) NOT NULL COMMENT '매장명',
    phone VARCHAR(30) NOT NULL COMMENT '전화번호',
    description TEXT NOT NULL COMMENT '매장 설명',
    business_number VARCHAR(30) NOT NULL COMMENT '사업자번호',
    address VARCHAR(255) NOT NULL COMMENT '주소',
    dong VARCHAR(50) NOT NULL COMMENT '동',
    latitude DOUBLE NOT NULL COMMENT '위도',
    longitude DOUBLE NOT NULL COMMENT '경도',

    location POINT GENERATED ALWAYS AS (
        ST_SRID(Point(longitude, latitude), 4326)
    ) STORED NOT NULL COMMENT '위치 좌표 (Point)',

    image_url VARCHAR(500) NOT NULL COMMENT '이미지 URL',
    weekday_open_time TIME NOT NULL COMMENT '평일 오픈 시간',
    weekday_close_time TIME NOT NULL COMMENT '평일 마감 시간',
    weekend_open_time TIME NOT NULL COMMENT '주말 오픈 시간',
    weekend_close_time TIME NOT NULL COMMENT '주말 마감 시간',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일시',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정일시',
    deleted_at DATETIME(6) NULL COMMENT '삭제일시 (소프트 삭제)',

    INDEX idx_member_id (member_id),
    INDEX idx_member_id_id (member_id, id),
    INDEX idx_store_category (store_category),
    INDEX idx_deleted_at (deleted_at),
    SPATIAL INDEX idx_location (location)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='매장 정보 테이블';
