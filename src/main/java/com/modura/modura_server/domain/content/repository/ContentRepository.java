package com.modura.modura_server.domain.content.repository;

import com.modura.modura_server.domain.content.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ContentRepository extends JpaRepository<Content, Long> {

    List<Content> findAllByTmdbIdIn(Collection<Integer> tmdbIds);
}
