package com.modura.modura_server.domain.place.repository;

import com.modura.modura_server.domain.place.entity.PlaceLikes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface PlaceLikesRepository extends JpaRepository<PlaceLikes, Long> {
    Boolean existsByUserIdAndPlaceId(Long userId, Long placeId);
    List<PlaceLikes> findByUserIdAndPlaceIdIn(Long userId, List<Long> placeIds);

    @Query("SELECT pl.place.id FROM PlaceLikes pl WHERE pl.user.id = :userId AND pl.place.id IN :placeIds")
    Set<Long> findIdsByUserIdAndPlaceIds(@Param("userId") Long userId, @Param("placeIds") List<Long> placeIds);

    @Modifying
    void deleteByUserIdAndPlaceId(Long userId, Long placeId);
}
