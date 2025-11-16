package com.modura.modura_server.domain.user.repository;

import com.modura.modura_server.domain.content.entity.Content;
import com.modura.modura_server.domain.user.entity.Stillcut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface StillcutRepository extends JpaRepository<Stillcut, Long> {

    List<Stillcut> findByContentId(Long contentId);
    List<Stillcut> findByPlaceId(Long placeId);

    @Query("SELECT s FROM Stillcut s JOIN FETCH s.place WHERE s.content IN :contents")
    List<Stillcut> findWithPlaceByContentIn(@Param("contents") List<Content> contents);

    @Query("SELECT s FROM Stillcut s JOIN FETCH s.content c WHERE s.place.id = :placeId")
    List<Stillcut> findByPlaceIdWithContent(@Param("placeId") Long placeId);

    @Query("SELECT s FROM Stillcut s JOIN FETCH s.content c WHERE s.place.id IN :placeIds")
    List<Stillcut> findByPlaceIdInWithContent(@Param("placeIds") List<Long> placeIds);

    @Modifying
    @Query("DELETE FROM Stillcut s WHERE s.content.id IN :contentIds")
    void deleteByContentIdIn(@Param("contentIds") Collection<Long> contentIds);
}
