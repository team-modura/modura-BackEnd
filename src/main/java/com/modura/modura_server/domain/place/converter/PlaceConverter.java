package com.modura.modura_server.domain.place.converter;

import com.modura.modura_server.domain.place.dto.PlaceResponseDTO;
import com.modura.modura_server.domain.place.entity.Place;
import com.modura.modura_server.domain.place.entity.PlaceReview;
import com.modura.modura_server.domain.user.entity.User;

import java.util.List;

public class PlaceConverter {
    public static PlaceResponseDTO.ReviewItemDTO toGetPlaceReviewDTO(PlaceReview placeReview, List<String> imageUrlList) {

        User user = placeReview.getUser();
        String username = (user.isInactive()) ? "탈퇴한 회원" : user.getNickname();

        return PlaceResponseDTO.ReviewItemDTO.builder()
                .placeReviewId(placeReview.getId())
                .username(username)
                .rating(placeReview.getRating())
                .comment(placeReview.getBody())
                .imageUrl(imageUrlList)
                .createdAt(placeReview.getCreatedAt().toString())
                .build();
    }

    public static PlaceResponseDTO.GetPlaceReviewListDTO toGetPlaceReviewListDTO(List<PlaceResponseDTO.ReviewItemDTO> placeReviewDTOList) {

        return PlaceResponseDTO.GetPlaceReviewListDTO.builder()
                .placeReviewList(placeReviewDTOList)
                .build();
    }

    public static PlaceResponseDTO.GetPlaceDetailDTO toGetPlaceDetailDTO(
            Place place,
            Boolean isLiked,
            Double reviewAvg,
            Integer reviewCount,
            List<PlaceResponseDTO.ContentItemDTO> contentList,
            List<PlaceResponseDTO.ReviewItemDTO> reviews
    ){

        return PlaceResponseDTO.GetPlaceDetailDTO.builder()
                .placeId(place.getId())
                .name(place.getName())
                .reviewAvg(reviewAvg)
                .latitude(place.getLatitude())
                .longitude(place.getLongitude())
                .isLiked(isLiked)
                .placeImageUrl(place.getThumbnail())
                .reviewCount(reviewCount)
                .contentList(contentList)
                .reviews(reviews)
                .build();
    }

    public static PlaceResponseDTO.GetPlaceListDTO toGetPlaceListDTO(List<PlaceResponseDTO.GetPlaceDTO> placeDTOList){

        return PlaceResponseDTO.GetPlaceListDTO.builder()
                .placeList(placeDTOList)
                .build();
    }

    public static PlaceResponseDTO.GetPlaceDTO toGetPlaceDTO(Place place,
                                                             boolean isLiked,
                                                             Double reviewAvg,
                                                             Integer reviewCount,
                                                             List<String> contentTitles) {

        return PlaceResponseDTO.GetPlaceDTO.builder()
                .id(place.getId())
                .name(place.getName())
                .isLiked(isLiked)
                .thumbnail(place.getThumbnail())
                .rating(reviewAvg)
                .reviewCount(reviewCount)
                .latitude(place.getLatitude())
                .longitude(place.getLongitude())
                .content(contentTitles)
                .build();
    }
}
