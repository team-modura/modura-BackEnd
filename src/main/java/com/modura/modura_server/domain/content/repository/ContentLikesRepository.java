package com.modura.modura_server.domain.content.repository;

import com.modura.modura_server.domain.content.entity.ContentLikes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface ContentLikesRepository extends JpaRepository<ContentLikes, Long> {

    @Query("SELECT cl.content.id FROM ContentLikes cl WHERE cl.user.id = :userId AND cl.content.id IN :contentIds")
    Set<Long> findIdsByUserIdAndContentIds(@Param("userId") Long userId, @Param("contentIds") List<Long> contentIds);
}
