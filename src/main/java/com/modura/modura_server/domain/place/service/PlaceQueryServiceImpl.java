package com.modura.modura_server.domain.place.service;

import com.modura.modura_server.domain.place.converter.PlaceConverter;
import com.modura.modura_server.domain.place.dto.PlaceResponseDTO;
import com.modura.modura_server.domain.place.entity.PlaceReview;
import com.modura.modura_server.domain.place.entity.ReviewImage;
import com.modura.modura_server.domain.place.repository.PlaceRepository;
import com.modura.modura_server.domain.place.repository.PlaceReviewRepository;
import com.modura.modura_server.domain.place.repository.ReviewImageRepository;
import com.modura.modura_server.domain.user.converter.UserConverter;
import com.modura.modura_server.domain.user.entity.Stillcut;
import com.modura.modura_server.domain.user.repository.StillcutRepository;
import com.modura.modura_server.global.exception.BusinessException;
import com.modura.modura_server.global.response.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlaceQueryServiceImpl implements PlaceQueryService {

    private final StillcutRepository stillcutRepository;
    private final PlaceReviewRepository placeReviewRepository;
    private final PlaceRepository placeRepository;
    private final ReviewImageRepository reviewImageRepository;

    @Override
    @Transactional(readOnly = true)
    public PlaceResponseDTO.GetStillcutListDTO getStillcut(Long placeId) {

        List<Stillcut> stillcutList = stillcutRepository.findByPlaceIdWithContent(placeId);

        return UserConverter.toGetStillcutListDTO(stillcutList);
    }

    @Override
    @Transactional(readOnly = true)
    public PlaceResponseDTO.GetPlaceReviewDTO getPlaceReview(Long placeId, Long reviewId){
        placeRepository.findById(placeId)
            .orElseThrow(() -> new BusinessException(ErrorStatus.PLACE_NOT_FOUND));

        PlaceReview placeReview = placeReviewRepository.findByIdAndPlaceId(reviewId,placeId)
            .orElseThrow(() -> new BusinessException(ErrorStatus.PLACE_REVIEW_NOT_FOUND));

        List<ReviewImage> reviewImageList = reviewImageRepository.findByPlaceReviewId(placeReview.getId());

        List<String> imageUrlList = reviewImageList.stream()
            .map(ReviewImage::getImageUrl)
            .toList();

        return PlaceConverter.toGetPlaceReviewDTO(placeReview, imageUrlList);
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

        List<PlaceResponseDTO.GetPlaceReviewDTO> placeReviewDTOList = placeReviewList.stream()
                .map(placeReview -> {
                    List<String> imageUrlList = imagesByReviewId.getOrDefault(
                            placeReview.getId(),
                            Collections.emptyList()
                    );
                    return PlaceConverter.toGetPlaceReviewDTO(placeReview, imageUrlList);
                })
                .toList();

        return PlaceConverter.toGetPlaceReviewListDTO(placeReviewDTOList);
    }
}