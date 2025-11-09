package com.modura.modura_server.domain.content.controller;

import com.modura.modura_server.domain.content.dto.ContentResponseDTO;
import com.modura.modura_server.domain.content.service.ContentQueryService;
import com.modura.modura_server.global.security.CustomUserDetails;
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

    @GetMapping("/detail/{contentId}")
    public ResponseEntity<ContentResponseDTO.ContentDetailDTO> getContentDetail(
            @PathVariable(value = "contentId") Long contentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUser().getId();
        var dto = contentQueryService.getContentDetail(contentId,userId);
        return ResponseEntity.ok(dto);
    }
}