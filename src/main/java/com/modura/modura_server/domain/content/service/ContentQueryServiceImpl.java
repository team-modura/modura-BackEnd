package com.modura.modura_server.domain.content.service;

import com.modura.modura_server.domain.content.converter.ContentConverter;
import com.modura.modura_server.domain.content.dto.ContentResponseDTO;
import com.modura.modura_server.domain.content.entity.Content;
import com.modura.modura_server.domain.content.entity.ContentReview;
import com.modura.modura_server.domain.content.entity.Platform;
import com.modura.modura_server.domain.content.repository.*;
import com.modura.modura_server.domain.place.entity.Place;
import com.modura.modura_server.domain.place.repository.PlaceLikesRepository;
import com.modura.modura_server.domain.user.entity.Stillcut;
import com.modura.modura_server.domain.user.repository.StillcutRepository;
import com.modura.modura_server.global.exception.BusinessException;
import com.modura.modura_server.global.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.modura.modura_server.global.response.code.status.ErrorStatus;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContentQueryServiceImpl implements ContentQueryService {

    private final ContentRepository contentRepository;
    private final ContentCategoryRepository contentCategoryRepository;
    private final ContentReviewRepository contentReviewRepository;
    private final ContentLikesRepository contentLikesRepository;
    private final PlatformRepository platformRepository;
    private final PlaceLikesRepository placeLikesRepository;
    private final StillcutRepository stillcutRepository;
    private final S3Service s3Service;

    @Override
    @Transactional(readOnly = true)
    public ContentResponseDTO.ContentDetailDTO getContentDetail(Long contentId, Long userId) {

        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.CONTENT_NOT_FOUND));

        Boolean isLiked = contentLikesRepository.existsByUserIdAndContentId(userId,contentId);

        RatingStatistics stats = calculateRatingStatistics(contentId);
        Double reviewAvg = contentReviewRepository.findAverageRatingByContentId(contentId);

        List<ContentReview> reviews = getReviewsByContent(content);
        List<String> categoryNames = getCategoryNames(content);
        List<String> platformNames = getPlatformNames(content);

        List<ContentResponseDTO.StillCutPlaceItemDTO> placeDTOs = getRelatedPlaces(content.getId(), userId);

        return ContentConverter.toContentDetailDTO(
                content,
                isLiked,
                categoryNames,
                platformNames,
                reviewAvg,
                stats.fiveStar, stats.fourStar, stats.threeStar, stats.twoStar, stats.oneStar,
                reviews,
                placeDTOs
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ContentResponseDTO.ReviewListDTO getContentReviewList(Long contentId) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.CONTENT_NOT_FOUND));

        List<ContentReview> reviews = Optional.ofNullable(
                contentReviewRepository.findByContent(content)
        ).orElse(List.of());
        return ContentConverter.toContentReviewListDTO(
                reviews
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ContentResponseDTO.ReviewItemDTO getContentReviewItem(Long contentId, Long reviewId) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.CONTENT_NOT_FOUND));

        ContentReview review = contentReviewRepository.findByIdAndContent(reviewId, content)
                .orElseThrow(() -> new BusinessException(ErrorStatus.CONTENT_REVIEW_NOT_FOUND));

        return ContentConverter.toContentReviewItemDTO(
                review
        );
    }

    private RatingStatistics calculateRatingStatistics(Long contentId) {
        List<Object[]> distribution = contentReviewRepository.findRatingDistribution(contentId);
        RatingStatistics stats = new RatingStatistics();

        for (Object[] row : distribution) {
            int rating = ((Number) row[0]).intValue();
            int count = ((Number) row[1]).intValue();
            stats.setCount(rating, count);
        }
        return stats;
    }

    private static class RatingStatistics {

        int fiveStar = 0;
        int fourStar = 0;
        int threeStar = 0;
        int twoStar = 0;
        int oneStar = 0;

        void setCount(int rating, int count) {
            switch (rating) {
                case 5 -> this.fiveStar = count;
                case 4 -> this.fourStar = count;
                case 3 -> this.threeStar = count;
                case 2 -> this.twoStar = count;
                case 1 -> this.oneStar = count;
            }
        }
    }

    private List<ContentReview> getReviewsByContent(Content content) {

        return Optional.ofNullable(contentReviewRepository.findByContent(content))
                .orElse(Collections.emptyList());
    }

    private List<String> getCategoryNames(Content content) {

        return Optional.ofNullable(contentCategoryRepository.findByContent(content))
                .orElse(Collections.emptyList())
                .stream()
                .map(cc -> cc.getCategory().getName())
                .collect(Collectors.toList());
    }

    private List<String> getPlatformNames(Content content) {

        return Optional.ofNullable(platformRepository.findByContent(content))
                .orElse(Collections.emptyList())
                .stream()
                .map(Platform::getName)
                .collect(Collectors.toList());
    }

    private List<ContentResponseDTO.StillCutPlaceItemDTO> getRelatedPlaces(Long contentId, Long userId) {

        List<Stillcut> stillcuts = Optional.ofNullable(stillcutRepository.findByContentId(contentId))
                .orElse(Collections.emptyList());

        if (stillcuts.isEmpty()) {
            return Collections.emptyList();
        }

        // 중복 제거된 Place ID 목록 추출
        List<Long> placeIds = stillcuts.stream()
                .map(sc -> sc.getPlace().getId())
                .distinct()
                .collect(Collectors.toList());

        // 유저가 좋아요한 장소 ID 일괄 조회 (Batch Fetch)
        Set<Long> likedPlaceIds = placeLikesRepository.findByUserIdAndPlaceIdIn(userId, placeIds).stream()
                .map(pl -> pl.getPlace().getId())
                .collect(Collectors.toSet());

        return stillcuts.stream()
                .collect(Collectors.toMap(
                        sc -> sc.getPlace().getId(),
                        sc -> sc,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ))
                .values().stream()
                .map(stillcut -> {
                    Place place = stillcut.getPlace();
                    Boolean isPlaceLiked = likedPlaceIds.contains(place.getId());

                    String stillcutThumbnail = generateThumbnailUrl(stillcut.getImageUrl());

                    return ContentResponseDTO.StillCutPlaceItemDTO.builder()
                            .id(place.getId())
                            .name(place.getName())
                            .thumbnail(stillcutThumbnail)
                            .isLiked(isPlaceLiked)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private String generateThumbnailUrl(String s3Key) {

        if (StringUtils.hasText(s3Key)) {
            return s3Service.generateViewPresignedUrl(s3Key);
        }
        return null;
    }
}