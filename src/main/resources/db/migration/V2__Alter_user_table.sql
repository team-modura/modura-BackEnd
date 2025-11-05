CREATE TABLE `users` (
                         `id` bigint NOT NULL AUTO_INCREMENT,
                         `created_at` datetime(6) NOT NULL,
                         `updated_at` datetime(6) NOT NULL,
                         `birth` varchar(255) NOT NULL,
                         `gender` enum('FEMALE','MALE','NONE') NOT NULL,
                         `image` text,
                         `inactive_date` date DEFAULT NULL,
                         `name` varchar(255) NOT NULL,
                         `nickname` varchar(255) NOT NULL,
                         `oauth_id` varchar(255) DEFAULT NULL,
                         `phone` varchar(13) NOT NULL,
                         PRIMARY KEY (`id`)
);

-- 1. 'role' 컬럼 추가
ALTER TABLE users ADD COLUMN role enum('ROLE_USER','ROLE_ADMIN') NOT NULL;

-- 2. 'oauth_id' 컬럼 타입 변경 (String -> Long)
ALTER TABLE users MODIFY COLUMN oauth_id bigint;

-- 3. 컬럼 삭제
ALTER TABLE users
DROP COLUMN gender,
DROP COLUMN name,
DROP COLUMN birth,
DROP COLUMN phone,
DROP COLUMN image;