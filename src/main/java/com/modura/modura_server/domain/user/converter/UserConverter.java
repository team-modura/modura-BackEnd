package com.modura.modura_server.domain.user.converter;

import com.modura.modura_server.domain.content.entity.Content;
import com.modura.modura_server.domain.content.entity.ContentReview;
import com.modura.modura_server.domain.place.dto.PlaceResponseDTO;
import com.modura.modura_server.domain.place.entity.PlaceReview;
import com.modura.modura_server.domain.search.dto.SearchResponseDTO;
import com.modura.modura_server.domain.user.dto.UserResponseDTO;
import com.modura.modura_server.domain.user.entity.Stillcut;

import java.util.List;
import java.util.stream.Collectors;

public class UserConverter {

    public static SearchResponseDTO.SearchContentListDTO toGetLikedContentListDTO(List<Content> contentList){

        List<SearchResponseDTO.SearchContentDTO> contentDTOList = contentList.stream()
                .map(content -> SearchResponseDTO.SearchContentDTO.builder()
                        .id(content.getId())
                        .title(content.getTitleKr())
                        .isLiked(true)
                        .thumbnail(content.getThumbnail())
                        .build())
                .collect(Collectors.toList());

        return SearchResponseDTO.SearchContentListDTO.builder()
                .contentList(contentDTOList)
                .build();
    }

    public static PlaceResponseDTO.GetStillcutListDTO toGetStillcutListDTO(List<Stillcut> stillcutList){

        List<PlaceResponseDTO.GetStillcutDTO> stillcutDTOList = stillcutList.stream()
                .map(stillcut -> {
                    Content content = stillcut.getContent();

                    return PlaceResponseDTO.GetStillcutDTO.builder()
                            .stillcutId(stillcut.getId())
                            .contentId(stillcut.getContent().getId())
                            .title(stillcut.getContent().getTitleKr())
                            .imageUrl(stillcut.getImageUrl())
                            .build();
                })
                .collect(Collectors.toList());

        return PlaceResponseDTO.GetStillcutListDTO.builder()
                .stillcutList(stillcutDTOList)
                .build();
    }

    public static UserResponseDTO.GetMyStillcutListDTO toGetMyStillcutListDTO(List<UserResponseDTO.GetMyStillcutDTO> stillcutDTOList){

        return UserResponseDTO.GetMyStillcutListDTO.builder()
                .stillcutList(stillcutDTOList)
                .build();
    }

    public static UserResponseDTO.GetContentReviewDTO toGetContentReviewDTO(ContentReview review) {

        return UserResponseDTO.GetContentReviewDTO.builder()
                .id(review.getId())
                .contentId(review.getContent().getId())
                .title(review.getContent().getTitleKr())
                .username(review.getUser().getNickname())
                .rating(review.getRating())
                .comment(review.getBody())
                .createdAt(review.getCreatedAt().toString())
                .thumbnail(review.getContent().getThumbnail())
                .build();
    }

    public static UserResponseDTO.GetPlaceReviewDTO toGetPlaceReviewDTO(PlaceReview review, List<String> imageUrlList, String thumbnailUrl) {

        return UserResponseDTO.GetPlaceReviewDTO.builder()
                .id(review.getId())
                .placeId(review.getPlace().getId())
                .name(review.getPlace().getName())
                .username(review.getUser().getNickname())
                .rating(review.getRating())
                .comment(review.getBody())
                .imageUrl(imageUrlList)
                .createdAt(review.getCreatedAt().toString())
                .thumbnail(thumbnailUrl)
                .build();
    }

    public static UserResponseDTO.GetReviewListDTO toGetReviewListDTO(List<UserResponseDTO.GetContentReviewDTO> contentReviewList,
                                                                      List<UserResponseDTO.GetPlaceReviewDTO> placeReviewList) {

        return UserResponseDTO.GetReviewListDTO.builder()
                .contentReviewList(contentReviewList)
                .placeReviewList(placeReviewList)
                .build();
    }
}
