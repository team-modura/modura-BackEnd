package com.modura.modura_server.domain.place.repository;

import com.modura.modura_server.domain.place.entity.PlaceLikes;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlaceLikesRepository extends JpaRepository<PlaceLikes, Long> {
    List<PlaceLikes> findByUserIdAndPlaceIdIn(Long userId, List<Long> placeIds);
}
