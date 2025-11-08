-- 1. 'plot' 컬럼 타입을 TEXT로 변경
ALTER TABLE `content`
    MODIFY COLUMN `plot` TEXT NULL;

-- 2. 'tmdb_id' 컬럼 추가
ALTER TABLE `content`
    ADD COLUMN `tmdb_id` int NULL;

-- 3. 'content' 테이블에서 'platform_id' 컬럼 삭제
ALTER TABLE `content`
DROP COLUMN `platform_id`;

-- 4. 'platform' 테이블 생성
CREATE TABLE `platform` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `created_at` datetime(6) NOT NULL,
    `updated_at` datetime(6) NOT NULL,
    `name` varchar(255) NOT NULL,
    `content_id` bigint NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UQ_NAME_CONTENT_ID` (`name`, `content_id`),
    KEY `FK_platform_content` (`content_id`),
    CONSTRAINT `FK_platform_content` FOREIGN KEY (`content_id`) REFERENCES `content` (`id`)
);