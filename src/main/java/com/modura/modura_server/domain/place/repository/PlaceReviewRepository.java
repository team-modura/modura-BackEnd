package com.modura.modura_server.domain.place.repository;

import com.modura.modura_server.domain.place.entity.PlaceReview;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlaceReviewRepository extends JpaRepository<PlaceReview, Long> {

    List<PlaceReview> findByPlaceId(Long placeId);

    Optional<PlaceReview> findByIdAndPlaceId(Long placeReviewId, Long placeId);

    @Query("SELECT COALESCE(ROUND(AVG(pr.rating), 1), 0) FROM PlaceReview pr WHERE pr.place.id = :placeId")
    Double findAverageRatingByPlaceId(@Param("placeId") Long placeId);

    @Query("SELECT pr FROM PlaceReview pr JOIN FETCH pr.user WHERE pr.place.id = :placeId")
    List<PlaceReview> findByPlace(@Param("placeId") Long placeId);

    @Query("SELECT COUNT(pr) FROM PlaceReview pr WHERE pr.place.id = :placeId")
    Integer countByPlace(@Param("placeId") Long placeId);

    Optional<PlaceReview> findByIdAndPlaceIdAndUserId(Long placeReviewId, Long placeId, Long userId);

    @Query("SELECT pr FROM PlaceReview pr JOIN FETCH pr.place p WHERE pr.user.id = :userId ORDER BY pr.createdAt DESC")
    List<PlaceReview> findAllByUserIdWithPlace(@Param("userId") Long userId);

    @Query("SELECT pr FROM PlaceReview pr WHERE pr.place.id IN :placeIds")
    List<PlaceReview> findByPlaceIdIn(@Param("placeIds") List<Long> placeIds);

    @Query("SELECT pr.place.id FROM PlaceReview pr GROUP BY pr.place.id ORDER BY COUNT(pr.id) DESC")
    List<Long> findTopPlaceIdsByReviewCount(Pageable pageable);
}
