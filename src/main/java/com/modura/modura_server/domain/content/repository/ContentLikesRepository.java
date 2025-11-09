package com.modura.modura_server.domain.content.repository;

import com.modura.modura_server.domain.content.entity.ContentLikes;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentLikesRepository extends JpaRepository<ContentLikes, Long> {
    Boolean existsByUserIdAndContentId(Long userId, Long contentId);
}
