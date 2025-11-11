package com.modura.modura_server.domain.place.repository;

import com.modura.modura_server.domain.place.entity.PlaceReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaceReviewRepository extends JpaRepository<PlaceReview, Long> {
}
