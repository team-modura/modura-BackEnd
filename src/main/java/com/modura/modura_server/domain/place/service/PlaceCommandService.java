package com.modura.modura_server.domain.place.service;

import com.modura.modura_server.domain.place.dto.PlaceRequestDTO;

public interface PlaceCommandService {
    void like(Long placeId, Long userId);
    void unlike(Long placeId, Long userId);
    void postStillcut(Long userId, Long placeId, Long stillcutId, PlaceRequestDTO.PostStillcutDTO request);
    Void postPlaceReview(Long userId, Long placeId, PlaceRequestDTO.PostPlaceReviewDTO request);
}
