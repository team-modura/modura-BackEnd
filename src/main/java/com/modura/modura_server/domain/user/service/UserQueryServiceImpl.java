package com.modura.modura_server.domain.user.service;

import com.modura.modura_server.domain.content.entity.Content;
import com.modura.modura_server.domain.content.entity.ContentReview;
import com.modura.modura_server.domain.content.repository.ContentRepository;
import com.modura.modura_server.domain.content.repository.ContentReviewRepository;
import com.modura.modura_server.domain.place.entity.Place;
import com.modura.modura_server.domain.place.entity.PlaceReview;
import com.modura.modura_server.domain.place.entity.ReviewImage;
import com.modura.modura_server.domain.place.repository.PlaceRepository;
import com.modura.modura_server.domain.place.repository.PlaceReviewRepository;
import com.modura.modura_server.domain.place.repository.ReviewImageRepository;
import com.modura.modura_server.domain.search.converter.SearchConverter;
import com.modura.modura_server.domain.search.dto.SearchResponseDTO;
import com.modura.modura_server.domain.user.converter.UserConverter;
import com.modura.modura_server.domain.user.dto.UserResponseDTO;
import com.modura.modura_server.domain.user.entity.Stillcut;
import com.modura.modura_server.domain.user.entity.UserStillcut;
import com.modura.modura_server.domain.user.repository.UserStillcutRepository;
import com.modura.modura_server.global.exception.BusinessException;
import com.modura.modura_server.global.response.code.status.ErrorStatus;
import com.modura.modura_server.global.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserQueryServiceImpl implements UserQueryService {

    private final ContentRepository contentRepository;
    private final PlaceRepository placeRepository;
    private final UserStillcutRepository userStillcutRepository;
    private final S3Service s3Service;
    private final ContentReviewRepository contentReviewRepository;
    private final PlaceReviewRepository placeReviewRepository;
    private final ReviewImageRepository reviewImageRepository;

    private static final Map<String, Integer> CONTENT_TYPE_MAP = Map.of(
            "series", 1,
            "movie", 2
    );

    @Override
    @Transactional(readOnly = true)
    public SearchResponseDTO.SearchContentListDTO getLikedContent(Long userId, String type) {

        Integer contentType = CONTENT_TYPE_MAP.get(type.toLowerCase());

        if (contentType == null) {
            return SearchResponseDTO.SearchContentListDTO.builder()
                    .contentList(Collections.emptyList())
                    .build();
        }

        List<Content> likedContents = contentRepository.findLikedContentsByUserAndType(userId, contentType);

        return UserConverter.toGetLikedContentListDTO(likedContents);
    }

    @Override
    @Transactional(readOnly = true)
    public SearchResponseDTO.SearchPlaceListDTO getLikedPlace(Long userId) {

        List<Place> likedPlaces = placeRepository.findLikedPlacesByUser(userId);

        List<SearchResponseDTO.SearchPlaceDTO> placeDTOList = likedPlaces.stream()
                .map(place -> {
                    // S3 Presigned URL 생성
                    String presignedUrl = null;
                    if (StringUtils.hasText(place.getThumbnail())) {
                        presignedUrl = s3Service.generateViewPresignedUrl(place.getThumbnail());
                    }

                    return SearchConverter.toSearchPlaceDTO(place, true, presignedUrl);
                })
                .collect(Collectors.toList());

        return SearchConverter.toSearchPlaceListDTO(placeDTOList);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO.GetMyStillcutListDTO getMyStillcutList(Long userId) {

        List<UserStillcut> stillcutList = userStillcutRepository.findAllWithDetailsByUserId(userId);

        List<UserResponseDTO.GetMyStillcutDTO> stillcutDTOList = stillcutList.stream()
                .map(userStillcut -> {
                    String s3Key = userStillcut.getImageUrl();
                    String presignedUrl = s3Service.generateViewPresignedUrl(s3Key);

                    return UserResponseDTO.GetMyStillcutDTO.builder()
                            .id(userStillcut.getId())
                            .imageUrl(presignedUrl)
                            .build();
                })
                .collect(Collectors.toList());

        return UserConverter.toGetMyStillcutListDTO(stillcutDTOList);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO.GetMyStillcutDetailDTO getMyStillcutDetail(Long userId, Long stillcutId) {

        UserStillcut userStillcut = userStillcutRepository.findUserDetailsById(userId, stillcutId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.USER_STILLCUT_NOT_FOUND));

        String s3Key = userStillcut.getImageUrl();
        String presignedUrl = s3Service.generateViewPresignedUrl(s3Key);

        Stillcut stillcut = userStillcut.getStillcut();
        Content content = stillcut.getContent();
        Place place = stillcut.getPlace();

        return UserResponseDTO.GetMyStillcutDetailDTO.builder()
                .id(userStillcut.getId())
                .imageUrl(presignedUrl)
                .stillcut(stillcut.getImageUrl())
                .title(content.getTitleKr())
                .name(place.getName())
                .date(userStillcut.getCreatedAt().toLocalDate().toString())
                .similarity(userStillcut.getSimilarity())
                .angle(userStillcut.getAngle())
                .clarity(userStillcut.getClarity())
                .color(userStillcut.getColor())
                .palette(userStillcut.getPalette())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO.GetReviewListDTO getReview(Long userId, String type) {

        List<UserResponseDTO.GetContentReviewDTO> contentReviewDTOs = Collections.emptyList();
        List<UserResponseDTO.GetPlaceReviewDTO> placeReviewDTOs = Collections.emptyList();

        switch (type) {
            case "all":
                contentReviewDTOs = loadContentReview(userId, null);
                placeReviewDTOs = loadPlaceReview(userId);
                break;
            case "series":
                contentReviewDTOs = loadContentReview(userId, 1);
                break;
            case "movie":
                contentReviewDTOs = loadContentReview(userId, 2);
                break;
            case "place":
                placeReviewDTOs = loadPlaceReview(userId);
                break;
            default:
                throw new BusinessException(ErrorStatus.INVALID_REVIEW_TYPE);
        }

        return UserConverter.toGetReviewListDTO(contentReviewDTOs, placeReviewDTOs);
    }

    private List<UserResponseDTO.GetContentReviewDTO> loadContentReview(Long userId, Integer contentType) {

        List<ContentReview> reviews;

        if (contentType == null) {
            reviews = contentReviewRepository.findAllByUserIdWithContent(userId);
        } else {
            reviews = contentReviewRepository.findAllByUserIdAndContentTypeWithContent(userId, contentType);
        }

        return reviews.stream()
                .map(UserConverter::toGetContentReviewDTO)
                .collect(Collectors.toList());
    }

    private List<UserResponseDTO.GetPlaceReviewDTO> loadPlaceReview(Long userId) {

        List<PlaceReview> placeReviews = placeReviewRepository.findAllByUserIdWithPlace(userId);

        if (placeReviews.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> reviewIds = placeReviews.stream()
                .map(PlaceReview::getId)
                .toList();

        List<ReviewImage> allImages = reviewImageRepository.findByPlaceReviewIdIn(reviewIds);

        Map<Long, List<String>> imagesByReviewId = allImages.stream()
                .collect(Collectors.groupingBy(
                        img -> img.getPlaceReview().getId(),
                        Collectors.mapping(ReviewImage::getImageUrl, Collectors.toList())
                ));

        return placeReviews.stream()
                .map(review -> {
                    List<String> s3Keys = imagesByReviewId.getOrDefault(review.getId(), Collections.emptyList());
                    List<String> imageUrls = s3Service.generateViewPresignedUrls(s3Keys);

                    String presignedUrl = null;
                    if (StringUtils.hasText(review.getPlace().getThumbnail())) {
                        presignedUrl = s3Service.generateViewPresignedUrl(review.getPlace().getThumbnail());
                    }

                    return UserConverter.toGetPlaceReviewDTO(review, imageUrls, presignedUrl);
                })
                .collect(Collectors.toList());
    }
}