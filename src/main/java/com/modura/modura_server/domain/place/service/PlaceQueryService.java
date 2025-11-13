package com.modura.modura_server.domain.place.service;

import com.modura.modura_server.domain.place.dto.PlaceResponseDTO;

public interface PlaceQueryService {

    PlaceResponseDTO.GetStillcutListDTO getStillcut(Long placeId);
    PlaceResponseDTO.GetPlaceReviewListDTO getPlaceReviewList(Long placeId);
    PlaceResponseDTO.ReviewItemDTO getPlaceReview(Long placeId, Long placeReviewId);
    PlaceResponseDTO.GetPlaceDetailDTO getPlaceDetail(Long placeId, Long userId);
}
