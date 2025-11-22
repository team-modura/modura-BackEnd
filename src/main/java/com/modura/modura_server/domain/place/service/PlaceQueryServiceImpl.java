package com.modura.modura_server.domain.place.service;

import com.modura.modura_server.domain.content.entity.Content;
import com.modura.modura_server.domain.content.repository.ContentLikesRepository;
import com.modura.modura_server.domain.content.repository.ContentRepository;
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
import com.modura.modura_server.domain.user.entity.User;
import com.modura.modura_server.domain.user.repository.StillcutRepository;
import com.modura.modura_server.global.exception.BusinessException;
import com.modura.modura_server.global.response.code.status.ErrorStatus;
import com.modura.modura_server.global.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
    private final ContentRepository contentRepository;
    private final S3Service s3Service;

    private static final String INACTIVE_USER_DISPLAY_NAME = "탈퇴한 회원";

    private static String resolveUsername(User user) {
        return (user == null || user.isInactive()) ? INACTIVE_USER_DISPLAY_NAME : user.getNickname();
    }

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

        String placeThumbnail = generateThumbnailUrl(place.getThumbnail());

        Boolean isLiked = placeLikesRepository.existsByUserIdAndPlaceId(userId, placeId);
        Double reviewAvg = placeReviewRepository.findAverageRatingByPlaceId(placeId);
        Integer reviewCount = placeReviewRepository.countByPlace(placeId);

        List<PlaceResponseDTO.ReviewItemDTO> reviewDTOs = getPlaceReviews(placeId);
        List<PlaceResponseDTO.ContentItemDTO> contentList = getRelatedContents(placeId, userId);

        return PlaceConverter.toGetPlaceDetailDTO(
                place,
                isLiked,
                placeThumbnail,
                reviewAvg,
                reviewCount,
                contentList,
                reviewDTOs
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PlaceResponseDTO.GetPlaceListDTO getPlace(Long userId, String query) {

        // 쿼리 유무에 따라 장소 목록 조회
        List<Place> places = findPlaces(query);

        if (places.isEmpty()) {
            return PlaceConverter.toGetPlaceListDTO(Collections.emptyList());
        }

        List<Long> placeIds = places.stream().map(Place::getId).toList();

        // 장소 '좋아요' 정보 일괄 조회
        Set<Long> likedPlaceIds = placeLikesRepository.findIdsByUserIdAndPlaceIds(userId, placeIds);

        // 리뷰 정보 일괄 조회 (평균, 개수 계산용)
        Map<Long, List<PlaceReview>> reviewsByPlaceId = placeReviewRepository.findByPlaceIdIn(placeIds)
                .stream()
                .collect(Collectors.groupingBy(review -> review.getPlace().getId()));

        // 연관된 컨텐츠 정보 일괄 조회 (스틸컷 기준)
        Map<Long, List<Stillcut>> stillcutsByPlaceId = stillcutRepository.findByPlaceIdInWithContent(placeIds)
                .stream()
                .collect(Collectors.groupingBy(stillcut -> stillcut.getPlace().getId()));

        List<PlaceResponseDTO.GetPlaceDTO> placeDTOList = places.stream()
                .map(place -> {
                    boolean isLiked = likedPlaceIds.contains(place.getId());

                    List<PlaceReview> placeReviews = reviewsByPlaceId.getOrDefault(place.getId(), Collections.emptyList());
                    int reviewCount = placeReviews.size();
                    double reviewAvg = placeReviews.stream()
                            .mapToDouble(PlaceReview::getRating)
                            .average()
                            .orElse(0.0);
                    reviewAvg = Math.round(reviewAvg * 10.0) / 10.0; // 소수점 첫째 자리

                    List<String> contentTitles = stillcutsByPlaceId.getOrDefault(place.getId(), Collections.emptyList())
                            .stream()
                            .map(stillcut -> stillcut.getContent().getTitleKr())
                            .distinct()
                            .toList();

                    // S3 Presigned URL 생성
                    String presignedUrl = null;
                    if (StringUtils.hasText(place.getThumbnail())) {
                        presignedUrl = s3Service.generateViewPresignedUrl(place.getThumbnail());
                    }

                    return PlaceConverter.toGetPlaceDTO(place, isLiked, presignedUrl, reviewAvg, reviewCount, contentTitles);
                }).toList();

        return PlaceConverter.toGetPlaceListDTO(placeDTOList);
    }

    private String generateThumbnailUrl(String s3Key) {

        if (StringUtils.hasText(s3Key)) {
            return s3Service.generateViewPresignedUrl(s3Key);
        }
        return null;
    }

    private List<PlaceResponseDTO.ReviewItemDTO> getPlaceReviews(Long placeId) {

        List<PlaceReview> reviews = placeReviewRepository.findByPlace(placeId);

        if (reviews.isEmpty()) {
            return Collections.emptyList();
        }

        // 리뷰 이미지 일괄 조회 (N+1 문제 방지)
        Map<Long, List<String>> imagesByReviewId = getReviewImagesMap(reviews);

        return reviews.stream()
                .map(review -> {
                    List<String> s3Keys = imagesByReviewId.getOrDefault(review.getId(), Collections.emptyList());
                    List<String> imageUrls = s3Service.generateViewPresignedUrls(s3Keys);
                    String username = resolveUsername(review.getUser());

                    return PlaceResponseDTO.ReviewItemDTO.builder()
                            .placeReviewId(review.getId())
                            .username(username)
                            .rating(review.getRating())
                            .comment(review.getBody())
                            .imageUrl(imageUrls)
                            .createdAt(review.getCreatedAt().toString())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private Map<Long, List<String>> getReviewImagesMap(List<PlaceReview> reviews) {

        List<Long> reviewIds = reviews.stream()
                .map(PlaceReview::getId)
                .toList();

        List<ReviewImage> allImages = reviewImageRepository.findByPlaceReviewIdIn(reviewIds);

        return allImages.stream()
                .collect(Collectors.groupingBy(
                        img -> img.getPlaceReview().getId(),
                        Collectors.mapping(ReviewImage::getImageUrl, Collectors.toList())
                ));
    }

    private List<PlaceResponseDTO.ContentItemDTO> getRelatedContents(Long placeId, Long userId) {

        List<Stillcut> stillcuts = stillcutRepository.findByPlaceId(placeId);

        if (stillcuts.isEmpty()) {
            return Collections.emptyList();
        }

        // 중복 없는 Content ID 추출
        List<Long> contentIds = stillcuts.stream()
                .map(stillcut -> stillcut.getContent().getId())
                .distinct()
                .collect(Collectors.toList());

        // 유저가 찜한 컨텐츠 ID 조회 (Batch 조회)
        Set<Long> likedContentIds = contentLikesRepository
                .findByUserIdAndContentIdIn(userId, contentIds).stream()
                .map(contentLikes -> contentLikes.getContent().getId())
                .collect(Collectors.toSet());

        // DTO 매핑
        return stillcuts.stream()
                .map(stillcut -> {
                    Long contentId = stillcut.getContent().getId();
                    return PlaceResponseDTO.ContentItemDTO.builder()
                            .contentId(contentId)
                            .title(stillcut.getContent().getTitleKr())
                            .thumbnail(stillcut.getContent().getThumbnail())
                            .isLiked(likedContentIds.contains(contentId))
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<Place> findPlaces(String query) {
        if (StringUtils.hasText(query)) {
            List<Place> placesByName = placeRepository.searchByNameContaining(query);
            List<Place> placesByContent = findPlacesByContentTitle(query);

            return combinePlaces(placesByName, placesByContent);
        } else {
            return placeRepository.findAll();
        }
    }

    private List<Place> findPlacesByContentTitle(String query) {

        List<Content> contents = contentRepository.searchByTitleContaining(query);
        if (contents.isEmpty()) {
            return Collections.emptyList();
        }

        List<Stillcut> stillcuts = stillcutRepository.findWithPlaceByContentIn(contents);

        // Stillcut에서 Place 추출 (중복 제거)
        return stillcuts.stream()
                .map(Stillcut::getPlace)
                .distinct()
                .toList();
    }

    private List<Place> combinePlaces(List<Place> placesByName, List<Place> placesByContent) {
        // Map을 사용하여 ID 기준 중복 제거
        Map<Long, Place> combinedPlaceMap = new LinkedHashMap<>();

        placesByName.forEach(place -> combinedPlaceMap.put(place.getId(), place));
        placesByContent.forEach(place -> combinedPlaceMap.putIfAbsent(place.getId(), place));

        return new ArrayList<>(combinedPlaceMap.values());
    }
}