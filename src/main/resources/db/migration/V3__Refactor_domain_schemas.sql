-- 1. 'content' 테이블 스키마 변경
ALTER TABLE `content`
    MODIFY COLUMN title_eng varchar(255) NULL,
    ADD COLUMN platform_id int NULL,
    ADD COLUMN `type` int NOT NULL DEFAULT 1,
    ADD COLUMN runtime int NULL;

-- 2. 'users' 테이블 스키마 변경
ALTER TABLE `users`
    ADD COLUMN address varchar(255) NULL;

-- 3. 'place' 테이블 스키마 변경
ALTER TABLE `place`
DROP FOREIGN KEY `FKmbcbqlq3e4w6aegphgtnet8yt`,
     DROP COLUMN content_id;

ALTER TABLE `place`
    ADD COLUMN thumbnail text NULL,
    ADD COLUMN latitude float NOT NULL DEFAULT 0.0,
    ADD COLUMN longitude float NOT NULL DEFAULT 0.0;

-- 4. 테이블 삭제
DROP TABLE `content_platform`;
DROP TABLE `platform`;
DROP TABLE `user_terms`;
DROP TABLE `terms`;

-- 5. 'stillcut' 새 테이블 생성
CREATE TABLE `stillcut` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `created_at` datetime(6) NOT NULL,
    `updated_at` datetime(6) NOT NULL,
    `image_url` text,
    `content_id` bigint NOT NULL,
    `place_id` bigint NOT NULL,
    PRIMARY KEY (`id`),
    KEY `FK_stillcut_content` (`content_id`),
    KEY `FK_stillcut_place` (`place_id`),
    CONSTRAINT `FK_stillcut_content` FOREIGN KEY (`content_id`) REFERENCES `content` (`id`),
    CONSTRAINT `FK_stillcut_place` FOREIGN KEY (`place_id`) REFERENCES `place` (`id`)
);

-- 6. 'user_stillcut' 새 테이블 생성
CREATE TABLE `user_stillcut` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `created_at` datetime(6) NOT NULL,
    `updated_at` datetime(6) NOT NULL,
    `image_url` text NOT NULL,
    `similarity` int NOT NULL,
    `angle` int NOT NULL,
    `clarity` int NOT NULL,
    `color` int NOT NULL,
    `palette` int NOT NULL,
    `user_id` bigint NOT NULL,
    `stillcut_id` bigint NOT NULL,
    PRIMARY KEY (`id`),
    KEY `FK_user_stillcut_user` (`user_id`),
    KEY `FK_user_stillcut_stillcut` (`stillcut_id`),
    CONSTRAINT `FK_user_stillcut_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
    CONSTRAINT `FK_user_stillcut_stillcut` FOREIGN KEY (`stillcut_id`) REFERENCES `stillcut` (`id`)
);

-- 7. 'user_category' 새 테이블 생성
CREATE TABLE `user_category` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `created_at` datetime(6) NOT NULL,
    `updated_at` datetime(6) NOT NULL,
    `user_id` bigint NOT NULL,
    `category_id` bigint NOT NULL,
    PRIMARY KEY (`id`),
    KEY `FK_user_category_user` (`user_id`),
    KEY `FK_user_category_category` (`category_id`),
    CONSTRAINT `FK_user_category_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
    CONSTRAINT `FK_user_category_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`)
);