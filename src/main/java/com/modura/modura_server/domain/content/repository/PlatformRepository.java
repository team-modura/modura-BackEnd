package com.modura.modura_server.domain.content.repository;

import com.modura.modura_server.domain.content.entity.Content;
import com.modura.modura_server.domain.content.entity.Platform;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlatformRepository extends JpaRepository<Platform, Long> {

    List<Platform> findByContent(Content content);
    List<Platform> findByContent_IdIn(List<Long> contentIds);
}
