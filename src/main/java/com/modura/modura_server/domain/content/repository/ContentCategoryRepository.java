package com.modura.modura_server.domain.content.repository;

import com.modura.modura_server.domain.content.entity.Content;
import com.modura.modura_server.domain.content.entity.ContentCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContentCategoryRepository extends JpaRepository<ContentCategory, Long> {
    List<ContentCategory> findByContent(Content content);
}
