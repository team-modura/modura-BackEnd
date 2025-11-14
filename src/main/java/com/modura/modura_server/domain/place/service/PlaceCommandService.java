package com.modura.modura_server.domain.place.service;

import com.modura.modura_server.domain.place.dto.PlaceRequestDTO;

import java.util.List;

public interface PlaceCommandService {
    void like(Long placeId, Long userId);
    void unlike(Long placeId, Long userId);
    Void postStillcut(Long userId, Long placeId, Long stillcutId, PlaceRequestDTO.PostStillcutDTO request);
    void postPlaceReview(Long userId, Long placeId, PlaceRequestDTO.PostPlaceReviewDTO request);
    void patchPlaceReview(Long userId, Long placeId, Long placeReviewId, PlaceRequestDTO.PatchPlaceReviewDTO request);
    void patchPlaceReviewImages(Long userId, Long placeId, Long placeReviewId, PlaceRequestDTO.ImageKeysDTO imageKey);
    void deletePlaceReviewImages(Long userId, Long placeId, Long placeReviewId, PlaceRequestDTO.ImageKeysDTO imageKey);
    void deletePlaceReview(Long userId, Long placeId, Long placeReviewId);
}
