package com.modura.modura_server.domain.place.service;

import com.modura.modura_server.domain.content.repository.ContentLikesRepository;
import com.modura.modura_server.domain.place.converter.PlaceConverter;
import com.modura.modura_server.domain.place.dto.PlaceResponseDTO;
import com.modura.modura_server.domain.place.entity.Place;
import com.modura.modura_server.domain.place.entity.PlaceReview;
import com.modura.modura_server.domain.place.entity.ReviewImage;
import com.modura.modura_server.domain.place.repository.PlaceLikesRepository;
import com.modura.modura_server.domain.place.repository.PlaceRepository;
import com.modura.modura_server.domain.place.repository.PlaceReviewRepository;
import com.modura.modura_server.domain.place.repository.ReviewImageRepository;
import com.modura.modura_server.domain.user.converter.UserConverter;
import com.modura.modura_server.domain.user.entity.Stillcut;
import com.modura.modura_server.domain.user.repository.StillcutRepository;
import com.modura.modura_server.global.exception.BusinessException;
import com.modura.modura_server.global.response.code.status.ErrorStatus;
import com.modura.modura_server.global.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlaceQueryServiceImpl implements PlaceQueryService {

    private final StillcutRepository stillcutRepository;
    private final PlaceReviewRepository placeReviewRepository;
    private final PlaceRepository placeRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final PlaceLikesRepository placeLikesRepository;
    private final ContentLikesRepository contentLikesRepository;
    private final S3Service s3Service;

    @Override
    @Transactional(readOnly = true)
    public PlaceResponseDTO.GetStillcutListDTO getStillcut(Long placeId) {

        List<Stillcut> stillcutList = stillcutRepository.findByPlaceIdWithContent(placeId);

        return UserConverter.toGetStillcutListDTO(stillcutList);
    }

    @Override
    @Transactional(readOnly = true)
    public PlaceResponseDTO.ReviewItemDTO getPlaceReview(Long placeId, Long reviewId){
        placeRepository.findById(placeId)
            .orElseThrow(() -> new BusinessException(ErrorStatus.PLACE_NOT_FOUND));

        PlaceReview placeReview = placeReviewRepository.findByIdAndPlaceId(reviewId,placeId)
            .orElseThrow(() -> new BusinessException(ErrorStatus.PLACE_REVIEW_NOT_FOUND));

        List<ReviewImage> reviewImageList = reviewImageRepository.findByPlaceReviewId(placeReview.getId());

        List<String> s3Keys = reviewImageList.stream()
            .map(ReviewImage::getImageUrl)
            .toList();

        List<String> imageUrls = s3Service.generateViewPresignedUrls(s3Keys);

        return PlaceConverter.toGetPlaceReviewDTO(placeReview, imageUrls);
    }

    @Override
    @Transactional(readOnly = true)
    public PlaceResponseDTO.GetPlaceReviewListDTO getPlaceReviewList(Long placeId) {
        placeRepository.findById(placeId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.PLACE_NOT_FOUND));

        List<PlaceReview> placeReviewList = placeReviewRepository.findByPlaceId(placeId);

        List<Long> reviewIds = placeReviewList.stream()
                .map(PlaceReview::getId)
                .toList();

        List<ReviewImage> allImages = reviewImageRepository.findByPlaceReviewIdIn(reviewIds);

        Map<Long, List<String>> imagesByReviewId = allImages.stream()
                .collect(Collectors.groupingBy(
                        img -> img.getPlaceReview().getId(),
                        Collectors.mapping(ReviewImage::getImageUrl, Collectors.toList())
                ));

        List<PlaceResponseDTO.ReviewItemDTO> placeReviewDTOList = placeReviewList.stream()
                .map(placeReview -> {
                    List<String> s3Keys = imagesByReviewId.getOrDefault(
                            placeReview.getId(),
                            Collections.emptyList()
                    );
                    List<String> imageUrlList = s3Service.generateViewPresignedUrls(s3Keys);
                    return PlaceConverter.toGetPlaceReviewDTO(placeReview, imageUrlList);
                })
                .toList();

        return PlaceConverter.toGetPlaceReviewListDTO(placeReviewDTOList);
    }

    @Override
    @Transactional(readOnly = true)
    public PlaceResponseDTO.GetPlaceDetailDTO getPlaceDetail(Long placeId, Long userId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.PLACE_NOT_FOUND));

        Boolean isLiked = placeLikesRepository.existsByUserIdAndPlaceId(userId, placeId);

        Double reviewAvg = placeReviewRepository.findAverageRatingByPlaceId(placeId);
        Integer reviewCount = placeReviewRepository.countByPlace(placeId);

        List<PlaceReview> reviews = Optional.ofNullable(
                placeReviewRepository.findByPlace(placeId)
        ).orElse(List.of());

        List<Stillcut> stillcuts = Optional.ofNullable(
                stillcutRepository.findByPlaceId(place.getId())
        ).orElse(List.of());

        List<Long> contentIds = stillcuts.stream()
                .map(stillcut -> stillcut.getContent().getId())
                .distinct()
                .collect(Collectors.toList());

        Set<Long> likedContentIds = contentLikesRepository
                .findByUserIdAndContentIdIn(userId, contentIds).stream()
                .map(contentLikes -> contentLikes.getContent().getId())
                .collect(Collectors.toSet());

        List<PlaceResponseDTO.ContentItemDTO> contentList = stillcuts.stream()
                .map(stillcut -> {
                    Long contentId = stillcut.getContent().getId();
                    Boolean isContentLiked = likedContentIds.contains(contentId);
                    return PlaceResponseDTO.ContentItemDTO.builder()
                            .contentId(contentId)
                            .title(stillcut.getContent().getTitleKr())
                            .thumbnail(stillcut.getContent().getThumbnail())
                            .isLiked(isContentLiked)
                            .build();
                })
                .collect(Collectors.toList());

        List<Long> reviewIds = reviews.stream()
                .map(PlaceReview::getId)
                .collect(Collectors.toList());

        List<ReviewImage> allImages = reviewImageRepository.findByPlaceReviewIdIn(reviewIds);
        Map<Long, List<String>> imagesByReviewId = allImages.stream()
                .collect(Collectors.groupingBy(
                        img -> img.getPlaceReview().getId(),
                        Collectors.mapping(ReviewImage::getImageUrl, Collectors.toList())
                ));

        List<PlaceResponseDTO.ReviewItemDTO> reviewDTOs = reviews.stream()
                .map(review -> {
                    List<String> s3Keys = imagesByReviewId.getOrDefault(
                            review.getId(),
                            Collections.emptyList()
                    );

                    List<String> imageUrls = s3Service.generateViewPresignedUrls(s3Keys);

                    return PlaceResponseDTO.ReviewItemDTO.builder()
                            .placeReviewId(review.getId())
                            .username(review.getUser().getNickname())
                            .rating(review.getRating())
                            .comment(review.getBody())
                            .imageUrl(imageUrls)
                            .createdAt(review.getCreatedAt().toString())
                            .build();
                })
                .collect(Collectors.toList());

        return PlaceConverter.toGetPlaceDetailDTO(
                place,
                isLiked,
                reviewAvg,
                reviewCount,
                contentList,
                reviewDTOs
        );
    }
}