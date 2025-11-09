package com.modura.modura_server.domain.content.controller;

import com.modura.modura_server.domain.content.dto.ContentRequestDTO;
import com.modura.modura_server.domain.content.dto.ContentResponseDTO;
import com.modura.modura_server.domain.content.repository.ContentLikesRepository;
import com.modura.modura_server.domain.content.service.ContentCommandService;
import com.modura.modura_server.domain.content.service.ContentQueryService;
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
@RequestMapping("/contents")
@Tag(name = "Content")
@Validated
public class ContentController {
    private final ContentQueryService contentQueryService;
    private final ContentCommandService contentCommandService;

    @Operation(summary = "컨텐츠 상세 조회")
    @GetMapping("/detail/{contentId}")
    public ApiResponse<ContentResponseDTO.ContentDetailDTO> getContentDetail(
            @PathVariable(value = "contentId") Long contentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUser().getId();
        var dto = contentQueryService.getContentDetail(contentId,userId);
        return ApiResponse.onSuccess(dto);
    }

    @Operation(summary = "컨텐츠 좋아요 하기")
    @PostMapping("{contentId}/like")
    public ApiResponse<Void> postLikeContent(
            @PathVariable(value="contentId") Long contentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        contentCommandService.like(contentId,userId);
        return ApiResponse.onSuccess(null);
    }

    @Operation(summary = "컨텐츠 좋아요 취소")
    @DeleteMapping("{contentId}/like")
    public ApiResponse<Void> deleteLikeContent(
            @PathVariable(value="contentId") Long contentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        contentCommandService.unlike(contentId,userId);
        return ApiResponse.onSuccess(null);
    }

    @Operation(summary = "컨텐츠 리뷰 전체 조회")
    @GetMapping("{contentId}/reviews")
    public ApiResponse<ContentResponseDTO.ReviewListDTO> getContentReviews(
            @PathVariable(value="contentId") Long contentId) {
        var dto = contentQueryService.getContentReviewList(contentId);
        return ApiResponse.onSuccess(dto);
    }

    @Operation(summary = "컨텐츠 리뷰 조회")
    @GetMapping("{contentId}/reviews/{reviewId}")
    public ApiResponse<ContentResponseDTO.ReviewItemDTO> getContentReview(
            @PathVariable(value="contentId") Long contentId,
            @PathVariable(value="reviewId") Long reviewId) {
        var dto = contentQueryService.getContentReviewItem(contentId, reviewId);
        return ApiResponse.onSuccess(dto);
    }

    @Operation(summary = "컨텐츠 리뷰 작성")
    @PostMapping("{contentId}/reviews")
    public ApiResponse<Void> postContentReview(
            @PathVariable(value="contentId") Long contentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ContentRequestDTO.ReviewReqDTO contentReviewReqDTO) {
        Long userId = userDetails.getUser().getId();
        contentCommandService.postContentReview(contentId, userId,contentReviewReqDTO);
        return ApiResponse.onSuccess(null);
    }

    @Operation(summary = "컨텐츠 리뷰 수정")
    @PatchMapping("{contentId}/reviews/{reviewId}")
    public ApiResponse<Void> patchContentReview(
            @PathVariable(value="contentId") Long contentId,
            @PathVariable(value="reviewId") Long reviewId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ContentRequestDTO.ReviewUpdateReqDTO contentReviewReqDTO) {
        Long userId = userDetails.getUser().getId();
        contentCommandService.patchContentReview(contentId, reviewId, userId, contentReviewReqDTO);
        return ApiResponse.onSuccess(null);
    }

    @Operation(summary = "컨텐츠 리뷰 삭제")
    @DeleteMapping("{contentId}/reviews/{reviewId}")
    public ApiResponse<Void> deleteContentReview(
            @PathVariable(value="contentId") Long contentId,
            @PathVariable(value="reviewId") Long reviewId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        contentCommandService.deleteContentReview(contentId, reviewId, userId);
        return ApiResponse.onSuccess(null);
    }
}