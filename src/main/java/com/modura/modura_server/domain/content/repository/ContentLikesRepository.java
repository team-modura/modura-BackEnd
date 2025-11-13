package com.modura.modura_server.domain.content.repository;

import com.modura.modura_server.domain.content.entity.ContentLikes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface ContentLikesRepository extends JpaRepository<ContentLikes, Long> {
    Boolean existsByUserIdAndContentId(Long userId, Long contentId);
  
  @Query("SELECT cl.content.id FROM ContentLikes cl WHERE cl.user.id = :userId AND cl.content.id IN :contentIds")
    Set<Long> findIdsByUserIdAndContentIds(@Param("userId") Long userId, @Param("contentIds") List<Long> contentIds);

  @Modifying
  void deleteByUserIdAndContentId(Long userId, Long contentId);

  List<ContentLikes> findByUserIdAndContentIdIn(Long userId, List<Long> contentIds);
}
