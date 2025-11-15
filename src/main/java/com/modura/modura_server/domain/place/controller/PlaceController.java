package com.modura.modura_server.domain.place.controller;

import com.modura.modura_server.domain.place.dto.PlaceRequestDTO;
import com.modura.modura_server.domain.place.dto.PlaceResponseDTO;
import com.modura.modura_server.domain.place.service.PlaceCommandService;
import com.modura.modura_server.domain.place.service.PlaceQueryService;
import com.modura.modura_server.global.response.ApiResponse;
import com.modura.modura_server.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/places")
@Tag(name = "Place")
@Validated
public class PlaceController {

    private final PlaceCommandService placeCommandService;
    private final PlaceQueryService placeQueryService;

    @Operation(summary = "촬영지 찜 하기")
    @PostMapping("{placeId}/like")
    public ApiResponse<Void> postLikePlace(
            @PathVariable(value="placeId") Long placeId,
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) {
        Long userId = userDetails.getUser().getId();
        placeCommandService.like(placeId,userId);
        return ApiResponse.onSuccess(null);
    }

    @Operation(summary = "촬영지 찜하기 취소")
    @DeleteMapping("{placeId}/unlike")
    public ApiResponse<Void> deletelikePlace(
            @PathVariable(value="placeId") Long placeId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        placeCommandService.unlike(placeId,userId);
        return ApiResponse.onSuccess(null);
    }

    @Operation(summary = "촬영지 리뷰 등록")
    @PostMapping("/{placeId}/reviews")
    public ApiResponse<Void> postPlaceReview(@AuthenticationPrincipal CustomUserDetails userDetails,
                                             @PathVariable Long placeId,
                                             @Valid @RequestBody PlaceRequestDTO.PostPlaceReviewDTO request) {

        Long userId = userDetails.getUser().getId();
        placeCommandService.postPlaceReview(userId, placeId, request);

        return ApiResponse.onSuccess(null);
    }

    @Operation(summary = "촬영지 리뷰 조회")
    @GetMapping("/{placeId}/reviews/{reviewId}")
    public ApiResponse<PlaceResponseDTO.ReviewItemDTO> getPlaceReview(@PathVariable Long placeId, @PathVariable Long reviewId) {
        PlaceResponseDTO.ReviewItemDTO response = placeQueryService.getPlaceReview(placeId, reviewId);

        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "촬영지 리뷰 전체 조회")
    @GetMapping("/{placeId}/reviews")
    public ApiResponse<PlaceResponseDTO.GetPlaceReviewListDTO> getPlaceReviewList(@PathVariable Long placeId) {

        PlaceResponseDTO.GetPlaceReviewListDTO response = placeQueryService.getPlaceReviewList(placeId);
        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "촬영지 리뷰 수정")
    @PatchMapping("/{placeId}/reviews/{reviewId}")
    public ApiResponse<Void> patchPlaceReview(@AuthenticationPrincipal CustomUserDetails userDetails,
                                              @PathVariable Long placeId, @PathVariable Long reviewId,
                                              @Valid @RequestBody PlaceRequestDTO.PatchPlaceReviewDTO request) {

        Long userId = userDetails.getUser().getId();
        placeCommandService.patchPlaceReview(userId, placeId, reviewId, request);

        return ApiResponse.onSuccess(null);
    }

    @Operation(summary = "촬영지 리뷰 이미지 추가")
    @PatchMapping("/{placeId}/reviews/{reviewId}/images")
    public ApiResponse<Void> patchPlaceReviewImages(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                    @PathVariable Long placeId, @PathVariable Long reviewId,
                                                    @Valid @RequestBody PlaceRequestDTO.ImageKeysDTO imageKeys) {

        Long userId = userDetails.getUser().getId();
        placeCommandService.patchPlaceReviewImages(userId, placeId, reviewId, imageKeys);

        return ApiResponse.onSuccess(null);
    }

    @Operation(summary = "촬영지 리뷰 이미지 삭제")
    @DeleteMapping("/{placeId}/reviews/{reviewId}/images")
    public ApiResponse<Void> deletePlaceReviewImages(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                     @PathVariable Long placeId, @PathVariable Long reviewId,
                                                     @Valid @RequestBody  PlaceRequestDTO.ImageKeysDTO imageKeys) {

        Long userId = userDetails.getUser().getId();
        placeCommandService.deletePlaceReviewImages(userId, placeId, reviewId, imageKeys);

        return ApiResponse.onSuccess(null);
    }

    @Operation(summary = "촬영지 리뷰 삭제")
    @DeleteMapping("/{placeId}/reviews/{reviewId}")
    public ApiResponse<Void> deletePlaceReview(@AuthenticationPrincipal CustomUserDetails userDetails,
                                               @PathVariable Long placeId, @PathVariable Long reviewId) {

        Long userId = userDetails.getUser().getId();
        placeCommandService.deletePlaceReview(userId, placeId, reviewId);

        return ApiResponse.onSuccess(null);
    }

    @Operation(summary = "촬영지 스틸컷 조회")
    @GetMapping("/{placeId}/stillcuts")
    public ApiResponse<PlaceResponseDTO.GetStillcutListDTO> getStillcut(@PathVariable Long placeId) {

        PlaceResponseDTO.GetStillcutListDTO response = placeQueryService.getStillcut(placeId);

        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "유저 스틸컷 저장")
    @PostMapping("/{placeId}/stillcuts/{stillcutId}")
    public ApiResponse<Void> postStillcut(@AuthenticationPrincipal CustomUserDetails userDetails,
                                          @PathVariable Long placeId, @PathVariable Long stillcutId,
                                          @Valid @RequestBody PlaceRequestDTO.PostStillcutDTO request) {

        Long userId = userDetails.getUser().getId();
        Void response = placeCommandService.postStillcut(userId, placeId, stillcutId, request);

        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "촬영지 상세보기 조회")
    @GetMapping("/{placeId}/detail")
    public ApiResponse<PlaceResponseDTO.GetPlaceDetailDTO> getPlaceDetail(
            @PathVariable Long placeId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUser().getId();
        PlaceResponseDTO.GetPlaceDetailDTO response = placeQueryService.getPlaceDetail(placeId, userId);

        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "촬영지 조회")
    @GetMapping
    public ApiResponse<PlaceResponseDTO.GetPlaceListDTO> getPlace(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                     @RequestParam(name = "query", required = false) String query) {

        Long userId = userDetails.getUser().getId();
        PlaceResponseDTO.GetPlaceListDTO response = placeQueryService.getPlace(userId, query);

        return ApiResponse.onSuccess(response);
    }
}