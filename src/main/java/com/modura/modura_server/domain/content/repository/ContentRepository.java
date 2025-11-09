package com.modura.modura_server.domain.content.repository;

import com.modura.modura_server.domain.content.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ContentRepository extends JpaRepository<Content, Long> {

    List<Content> findAllByTmdbIdIn(Collection<Integer> tmdbIds);

    @Query("SELECT c FROM Content c WHERE c.titleKr LIKE CONCAT('%', :query, '%') " +
            "OR LOWER(c.titleEng) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Content> searchByTitleContaining(@Param("query") String query);
}
