package com.modura.modura_server.domain.place.service;

import com.modura.modura_server.domain.place.dto.PlaceRequestDTO;
import com.modura.modura_server.domain.place.entity.Place;
import com.modura.modura_server.domain.place.entity.PlaceReview;
import com.modura.modura_server.domain.place.entity.ReviewImage;
import com.modura.modura_server.domain.place.repository.PlaceLikesRepository;
import com.modura.modura_server.domain.place.repository.PlaceRepository;
import com.modura.modura_server.domain.user.entity.Stillcut;
import com.modura.modura_server.domain.place.repository.PlaceReviewRepository;
import com.modura.modura_server.domain.place.repository.ReviewImageRepository;
import com.modura.modura_server.domain.user.entity.User;
import com.modura.modura_server.domain.user.entity.UserStillcut;
import com.modura.modura_server.domain.user.repository.StillcutRepository;
import com.modura.modura_server.domain.user.repository.UserRepository;
import com.modura.modura_server.domain.user.repository.UserStillcutRepository;
import com.modura.modura_server.global.exception.BusinessException;
import com.modura.modura_server.global.response.code.status.ErrorStatus;
import com.modura.modura_server.global.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaceCommandServiceImpl implements PlaceCommandService {

    private final PlaceRepository placeRepository;
    private final PlaceLikesRepository placeLikesRepository;
    private final UserRepository userRepository;
    private final StillcutRepository stillcutRepository;
    private final UserStillcutRepository userStillcutRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final PlaceReviewRepository placeReviewRepository;
    private final S3Service s3Service;

    @Override
    @Transactional
    public void like(Long placeId, Long userId) {
        if(placeLikesRepository.existsByUserIdAndPlaceId(userId, placeId)) {
            return;
        }

        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.PLACE_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.MEMBER_NOT_FOUND));

        placeLikesRepository.save(
                com.modura.modura_server.domain.place.entity.PlaceLikes.builder()
                        .place(place)
                        .user(user)
                        .build()
        );
    }

    @Override
    @Transactional
    public void unlike(Long placeId, Long userId) {
        if(!placeLikesRepository.existsByUserIdAndPlaceId(userId, placeId)) {
            return;
        }
        placeLikesRepository.deleteByUserIdAndPlaceId(userId, placeId);
    }

    @Override
    @Transactional
    public Void postStillcut(Long userId, Long placeId, Long stillcutId, PlaceRequestDTO.PostStillcutDTO request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.MEMBER_NOT_FOUND));

        Stillcut stillcut = stillcutRepository.findById(stillcutId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.STILLCUT_NOT_FOUND));

        if (!stillcut.getPlace().getId().equals(placeId)) {
            throw new BusinessException(ErrorStatus.STILLCUT_PLACE_MISMATCH);
        }

        UserStillcut newUserStillcut = UserStillcut.builder()
                .user(user)
                .stillcut(stillcut)
                .imageUrl(request.getImageUrl())
                .similarity(request.getSimilarity())
                .angle(request.getAngle())
                .clarity(request.getClarity())
                .color(request.getColor())
                .palette(request.getPalette())
                .build();

        userStillcutRepository.save(newUserStillcut);
        return null;
    }

    @Override
    @Transactional
    public void postPlaceReview(Long userId, Long placeId, PlaceRequestDTO.PostPlaceReviewDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.MEMBER_NOT_FOUND));

        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.PLACE_NOT_FOUND));

        PlaceReview placeReview = PlaceReview.builder()
                .user(user)
                .place(place)
                .rating(request.getRating())
                .body(request.getComment())
                .build();
        placeReviewRepository.save(placeReview);

        if (request.getImageUrl() != null) {
            for (String imageUrl : request.getImageUrl()) {
                ReviewImage reviewImage = ReviewImage.builder()
                   .placeReview(placeReview)
                   .imageUrl(imageUrl)
                   .build();
                reviewImageRepository.save(reviewImage);
            }
        }
    }

    @Override
    @Transactional
    public void patchPlaceReview(Long userId, Long placeId, Long placeReviewId, PlaceRequestDTO.PatchPlaceReviewDTO request) {
        PlaceReview placeReview = placeReviewRepository.findByIdAndPlaceIdAndUserId(placeReviewId, placeId, userId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.PLACE_REVIEW_NOT_FOUND));

        Integer newRating = request.getRating();
        String newComment = request.getComment();

        if (newRating != null) {
            placeReview.setRating(newRating);
        }
        if (newComment != null) {
            placeReview.setBody(newComment);
        }

        if (newRating == null && newComment == null) {
            throw new BusinessException(ErrorStatus.BAD_REQUEST);
        }

        placeReviewRepository.save(placeReview);
    }

    @Override
    @Transactional
    public void patchPlaceReviewImages(Long userId, Long placeId, Long placeReviewId, PlaceRequestDTO.ImageKeysDTO request) {
        PlaceReview placeReview = placeReviewRepository.findByIdAndPlaceIdAndUserId(placeReviewId, placeId, userId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.PLACE_REVIEW_NOT_FOUND));

        for (String key : request.getImageKeys()) {
            ReviewImage reviewImage = ReviewImage.builder()
                    .placeReview(placeReview)
                    .imageUrl(key)
                    .build();
            reviewImageRepository.save(reviewImage);
        }
    }

    @Override
    @Transactional
    public void deletePlaceReviewImages(Long userId, Long placeId, Long placeReviewId, PlaceRequestDTO.ImageKeysDTO request) {
        placeReviewRepository.findByIdAndPlaceIdAndUserId(placeReviewId, placeId, userId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.PLACE_REVIEW_NOT_FOUND));

        List<String> imageKeys = request.getImageKeys();
        List<ReviewImage> imagesToDelete = reviewImageRepository.findByPlaceReviewId(placeReviewId)
                .stream()
                .filter(image -> imageKeys.contains(image.getImageUrl()))
                .toList();

        List<String> imageKeysToDelete = imagesToDelete.stream()
                .map(ReviewImage::getImageUrl)
                .toList();

        s3Service.deleteAll(imageKeysToDelete);
        reviewImageRepository.deleteAll(imagesToDelete);
    }

    @Override
    @Transactional
    public void deletePlaceReview(Long userId, Long placeId, Long placeReviewId) {
        PlaceReview placeReview = placeReviewRepository.findByIdAndPlaceIdAndUserId(placeReviewId, placeId, userId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.PLACE_REVIEW_NOT_FOUND));

        if(!placeReview.getUser().getId().equals(userId) || !placeReview.getPlace().getId().equals(placeId)) {
            throw new BusinessException(ErrorStatus.FORBIDDEN);
        }

        List<ReviewImage> reviewImages = reviewImageRepository.findByPlaceReviewId(placeReviewId);
        List<String> imageKeys = reviewImages.stream()
                .map(ReviewImage::getImageUrl)
                .toList();
        s3Service.deleteAll(imageKeys);

        reviewImageRepository.deleteByPlaceReviewId(placeReviewId);
        placeReviewRepository.delete(placeReview);
    }
}