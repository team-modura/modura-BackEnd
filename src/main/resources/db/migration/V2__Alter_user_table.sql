-- 1. 'oauth_id' 컬럼 타입 변경 (String -> Long)
ALTER TABLE users MODIFY COLUMN oauth_id bigint;

-- 2. 컬럼 삭제
ALTER TABLE users
DROP COLUMN gender,
DROP COLUMN name,
DROP COLUMN birth,
DROP COLUMN phone,
DROP COLUMN image;

-- 3. 'role' 컬럼 추가
ALTER TABLE users ADD COLUMN role enum('ROLE_USER','ROLE_ADMIN') NOT NULL DEFAULT 'ROLE_USER';