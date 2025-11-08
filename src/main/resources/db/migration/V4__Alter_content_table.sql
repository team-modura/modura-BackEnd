-- 1. 'plot' 컬럼 타입을 TEXT로 변경
ALTER TABLE `content`
    MODIFY COLUMN `plot` TEXT NULL;

-- 2. 'tmdb_id' 컬럼 추가
ALTER TABLE `content`
    ADD COLUMN `tmdb_id` int  NULL;