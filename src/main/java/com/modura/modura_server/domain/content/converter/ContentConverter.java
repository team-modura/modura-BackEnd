package com.modura.modura_server.domain.content.converter;

import com.modura.modura_server.domain.content.dto.ContentResponseDTO;
import com.modura.modura_server.domain.content.entity.Content;
import com.modura.modura_server.domain.content.entity.ContentReview;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ContentConverter {
    public static ContentResponseDTO.ContentDetailDTO toContentDetailDTO(
            Content content,
            Boolean isLiked,
            List<String> contentCategories,
            List<String> platforms,
            Double reviewAvg,
            Integer fiveStarCount,
            Integer fourStarCount,
            Integer threeStarCount,
            Integer twoStarCount,
            Integer oneStarCount,
            List<ContentReview> reviews,
            List<ContentResponseDTO.StillCutPlaceItemDTO> placeDtos
    ) {
        List<ContentResponseDTO.ReviewItemDTO> reviewItemDTOS = reviews.stream()
                .sorted(Comparator.comparing(ContentReview::getCreatedAt).reversed())
                .filter(review -> review.getUser() != null)
                .limit(2)
                .map(review -> ContentResponseDTO.ReviewItemDTO.builder()
                        .id(review.getId())
                        .username(review.getUser().getNickname())
                        .rating(review.getRating())
                        .comment(review.getBody())
                        .createdAt(review.getCreatedAt().toString())
                        .build())
                .collect(Collectors.toList());


        return ContentResponseDTO.ContentDetailDTO.builder()
                .id(content.getId())
                .tmdbId(content.getTmdbId())
                .type(content.getType())
                .titleKr(content.getTitleKr())
                .titleEng(content.getTitleEng())
                .isLiked(isLiked)
                .runtime(content.getRuntime())
                .year(content.getYear())
                .contentCategories(contentCategories)
                .plot(content.getPlot())
                .thumbnail(content.getThumbnail())
                .platforms(platforms)
                .reviewAvg(reviewAvg)
                .fiveStarCount(fiveStarCount)
                .fourStarCount(fourStarCount)
                .threeStarCount(threeStarCount)
                .twoStarCount(twoStarCount)
                .oneStarCount(oneStarCount)
                .reviews(reviewItemDTOS)
                .places(placeDtos)
                .build();
    }

    public static ContentResponseDTO.ReviewListDTO toContentReviewListDTO(
            List<ContentReview> reviews
    ) {
        List<ContentResponseDTO.ReviewItemDTO> reviewItemDTOS = reviews.stream()
                .sorted(Comparator.comparing(ContentReview::getCreatedAt).reversed())
                .filter(review -> review.getUser() != null)
                .map(review -> ContentResponseDTO.ReviewItemDTO.builder()
                        .id(review.getId())
                        .username(review.getUser().getNickname())
                        .rating(review.getRating())
                        .comment(review.getBody())
                        .createdAt(review.getCreatedAt().toString())
                        .build())
                .collect(Collectors.toList());


        return ContentResponseDTO.ReviewListDTO.builder()
                .reviews(reviewItemDTOS)
                .build();
    }

    public static ContentResponseDTO.ReviewItemDTO toContentReviewItemDTO(ContentReview review) {
        if (review == null || review.getUser() == null) {
            return null;
        }

        return ContentResponseDTO.ReviewItemDTO.builder()
                .id(review.getId())
                .username(
                        review.getUser() != null && review.getUser().getNickname() != null ?
                                review.getUser().getNickname() : "알 수 없음"
                )
                .rating(review.getRating() != null ? review.getRating() : 0)
                .comment(review.getBody() != null ? review.getBody() : "")
                .createdAt(review.getCreatedAt() != null ? review.getCreatedAt().toString() : "")
                .build();
    }
}
