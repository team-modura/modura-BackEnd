package com.modura.modura_server.domain.content.service;

import com.modura.modura_server.domain.content.converter.ContentConverter;
import com.modura.modura_server.domain.content.dto.ContentResponseDTO;
import com.modura.modura_server.domain.content.entity.Content;
import com.modura.modura_server.domain.content.entity.ContentReview;
import com.modura.modura_server.domain.content.entity.Platform;
import com.modura.modura_server.domain.content.repository.*;
import com.modura.modura_server.domain.place.entity.Place;
import com.modura.modura_server.domain.place.entity.PlaceLikes;
import com.modura.modura_server.domain.place.repository.PlaceLikesRepository;
import com.modura.modura_server.domain.user.entity.Stillcut;
import com.modura.modura_server.domain.user.repository.StillcutRepository;
import com.modura.modura_server.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.modura.modura_server.global.response.code.status.ErrorStatus;

import java.util.List;
import java.util.Optional;
import java.util.Set;
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

    @Override
    @Transactional(readOnly = true)
    public ContentResponseDTO.ContentDetailDTO getContentDetail(Long contentId, Long userId) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.CONTENT_NOT_FOUND));

        Boolean isLiked = contentLikesRepository.existsByUserIdAndContentId(userId,contentId);

        List<Object[]> distribution = contentReviewRepository.findRatingDistribution(contentId);
        Double reviewAvg = contentReviewRepository.findAverageRatingByContentId(contentId);

        int fiveStarCount = 0, fourStarCount = 0, threeStarCount = 0, twoStarCount = 0, oneStarCount = 0;
        for(Object[] row : distribution) {
            int rating = ((Number)row[0]).intValue();
            int cnt = ((Number)row[1]).intValue();
            switch(rating) {
                case 5: fiveStarCount = cnt; break;
                case 4: fourStarCount = cnt; break;
                case 3: threeStarCount = cnt; break;
                case 2: twoStarCount = cnt; break;
                case 1: oneStarCount = cnt; break;
            }
        }
        List<ContentReview> reviews = Optional.ofNullable(
                contentReviewRepository.findByContent(content)
        ).orElse(List.of());

        List<String> categoryNames = Optional.ofNullable(
                        contentCategoryRepository.findByContent(content)
                ).orElse(List.of())
                .stream()
                .map(cc -> cc.getCategory().getName())
                .collect(Collectors.toList());

        List<String> platformNames = Optional.ofNullable(
                        platformRepository.findByContent(content)
                ).orElse(List.of())
                .stream()
                .map(Platform::getName)
                .collect(Collectors.toList());

        List<Stillcut> stillcuts = Optional.ofNullable(
                stillcutRepository.findByContentId(content.getId())
        ).orElse(List.of());

        List<Long> placeIds = stillcuts.stream()
                .map(sc -> sc.getPlace().getId())
                .collect(Collectors.toList());

        List<PlaceLikes> likedPlaces = placeLikesRepository.findByUserIdAndPlaceIdIn(userId, placeIds);

        Set<Long> likedPlaceIdSet = likedPlaces.stream()
                .map(pl -> pl.getPlace().getId())
                .collect(Collectors.toSet());

        List<ContentResponseDTO.StillCutPlaceItemDTO> placeDtos = stillcuts.stream()
                .map(stillcut -> {
                    Place place = stillcut.getPlace();
                    Boolean isPlaceLiked = likedPlaceIdSet.contains(place.getId());
                    return ContentResponseDTO.StillCutPlaceItemDTO.builder()
                            .id(stillcut.getId())
                            .name(place.getName())
                            .thumbnail(place.getThumbnail())
                            .isLiked(isPlaceLiked)
                            .build();
                })
                .collect(Collectors.toList());

        return ContentConverter.toContentDetailDTO(
                content,
                isLiked,
                categoryNames,
                platformNames,
                reviewAvg,
                fiveStarCount, fourStarCount, threeStarCount, twoStarCount, oneStarCount,
                reviews,
                placeDtos
        );
    }
}