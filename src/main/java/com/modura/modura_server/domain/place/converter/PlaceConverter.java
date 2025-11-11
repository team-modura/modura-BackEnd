package com.modura.modura_server.domain.place.converter;

import com.modura.modura_server.domain.place.dto.PlaceResponseDTO;
import com.modura.modura_server.domain.place.entity.PlaceReview;

import java.util.List;

public class PlaceConverter {
    public static PlaceResponseDTO.GetPlaceReviewDTO toGetPlaceReviewDTO(PlaceReview placeReview, List<String> imageUrlList) {

        return PlaceResponseDTO.GetPlaceReviewDTO.builder()
                .placeReviewId(placeReview.getId())
                .rating(placeReview.getRating())
                .comment(placeReview.getBody())
                .imageUrl(imageUrlList)
                .build();
    }

    public static PlaceResponseDTO.GetPlaceReviewListDTO toGetPlaceReviewListDTO(List<PlaceResponseDTO.GetPlaceReviewDTO> placeReviewDTOList) {

        return PlaceResponseDTO.GetPlaceReviewListDTO.builder()
                .placeReviewList(placeReviewDTOList)
                .build();
    }
}
