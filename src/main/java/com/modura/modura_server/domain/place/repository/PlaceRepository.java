package com.modura.modura_server.domain.place.repository;

import com.modura.modura_server.domain.place.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    @Query("SELECT p FROM Place p WHERE p.name LIKE CONCAT('%', :query, '%')")
    List<Place> searchByNameContaining(@Param("query") String query);

    @Query("SELECT p FROM Place p " +
            "JOIN PlaceLikes pl ON p.id = pl.place.id " +
            "WHERE pl.user.id = :userId")
    List<Place> findLikedPlacesByUser(@Param("userId") Long userId);
}
