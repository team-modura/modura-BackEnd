package com.modura.modura_server.domain.search.controller;

import com.modura.modura_server.domain.search.dto.SearchResponseDTO;
import com.modura.modura_server.domain.search.service.SearchQueryService;
import com.modura.modura_server.global.response.ApiResponse;
import com.modura.modura_server.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/search")
@Tag(name = "Search")
@Validated
public class SearchController {

    private final SearchQueryService searchQueryService;

    @Operation(summary = "컨텐츠 검색")
    @GetMapping("/contents")
    public ApiResponse<List<SearchResponseDTO.SearchContentDTO>> searchContent(@AuthenticationPrincipal CustomUserDetails userDetails,
                                        @RequestParam(name = "query") @NotBlank String query) {

        Long userId = userDetails.getUser().getId();
        List<SearchResponseDTO.SearchContentDTO> response = searchQueryService.searchContent(userId, query);

        return ApiResponse.onSuccess(response);
    }
}