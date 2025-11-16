package com.modura.modura_server.domain.content.repository;

import com.modura.modura_server.domain.content.entity.Content;
import com.modura.modura_server.domain.content.entity.ContentCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ContentCategoryRepository extends JpaRepository<ContentCategory, Long> {
    List<ContentCategory> findByContent(Content content);

    @Modifying
    @Query("DELETE FROM ContentCategory cc WHERE cc.content.id IN :contentIds")
    void deleteByContentIdIn(@Param("contentIds") Collection<Long> contentIds);
}
