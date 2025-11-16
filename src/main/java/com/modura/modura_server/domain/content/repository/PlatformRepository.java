package com.modura.modura_server.domain.content.repository;

import com.modura.modura_server.domain.content.entity.Content;
import com.modura.modura_server.domain.content.entity.Platform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface PlatformRepository extends JpaRepository<Platform, Long> {

    List<Platform> findByContent(Content content);
    List<Platform> findByContent_IdIn(List<Long> contentIds);

    @Modifying
    @Query("DELETE FROM Platform p WHERE p.content.id IN :contentIds")
    void deleteByContentIdIn(@Param("contentIds") Collection<Long> contentIds);
}
