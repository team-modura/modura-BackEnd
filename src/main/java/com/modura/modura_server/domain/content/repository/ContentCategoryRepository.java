package com.modura.modura_server.domain.content.repository;

import com.modura.modura_server.domain.content.entity.ContentCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentCategoryRepository extends JpaRepository<ContentCategory, Long> {

}
