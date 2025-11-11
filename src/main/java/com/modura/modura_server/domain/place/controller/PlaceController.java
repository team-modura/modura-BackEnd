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

    @Operation(summary = "장소 찜 하기")
    @PostMapping("{placeId}/like")
    public ApiResponse<Void> postLikePlace(
            @PathVariable(value="placeId") Long placeId,
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) {
        Long userId = userDetails.getUser().getId();
        placeCommandService.like(placeId,userId);
        return ApiResponse.onSuccess(null);
    }

    @Operation(summary = "장소 찜 취소")
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
    public ApiResponse<PlaceResponseDTO.GetPlaceReviewDTO> getPlaceReview(@PathVariable Long placeId, @PathVariable Long reviewId) {
        PlaceResponseDTO.GetPlaceReviewDTO response = placeQueryService.getPlaceReview(placeId, reviewId);

        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "촬영지 리뷰 목록 조회")
    @GetMapping("/{placeId}/reviews")
    public ApiResponse<PlaceResponseDTO.GetPlaceReviewListDTO> getPlaceReviewList(@PathVariable Long placeId) {

        PlaceResponseDTO.GetPlaceReviewListDTO response = placeQueryService.getPlaceReviewList(placeId);
        return ApiResponse.onSuccess(response);
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
}