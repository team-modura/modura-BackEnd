package com.modura.modura_server.domain.place.repository;

import com.modura.modura_server.domain.place.entity.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {
    List<ReviewImage> findByPlaceReviewId(Long placeReviewId);
    List<ReviewImage> findByPlaceReviewIdIn(List<Long> placeReviewIds);

    void deleteByPlaceReviewId(Long placeReviewIds);
}
