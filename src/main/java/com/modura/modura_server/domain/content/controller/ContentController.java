package com.modura.modura_server.domain.content.controller;

import com.modura.modura_server.domain.content.dto.ContentResponseDTO;
import com.modura.modura_server.domain.content.repository.ContentLikesRepository;
import com.modura.modura_server.domain.content.service.ContentCommandService;
import com.modura.modura_server.domain.content.service.ContentQueryService;
import com.modura.modura_server.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    private final ContentLikesRepository contentLikesRepository;
    private final ContentCommandService contentCommandService;

    @Operation(summary = "컨텐츠 상세 조회")
    @GetMapping("/detail/{contentId}")
    public ResponseEntity<ContentResponseDTO.ContentDetailDTO> getContentDetail(
            @PathVariable(value = "contentId") Long contentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUser().getId();
        var dto = contentQueryService.getContentDetail(contentId,userId);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "컨텐츠 좋아요 하기")
    @PostMapping("{contentId}/like")
    public ResponseEntity<?> postLikeContent(
            @PathVariable(value="contentId") Long contentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        contentCommandService.like(contentId,userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "컨텐츠 좋아요 취소")
    @DeleteMapping("{contentId}/like")
    public ResponseEntity<?> deleteLikeContent(
            @PathVariable(value="contentId") Long contentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        contentCommandService.unlike(contentId,userId);
        return ResponseEntity.ok().build();
    }

}