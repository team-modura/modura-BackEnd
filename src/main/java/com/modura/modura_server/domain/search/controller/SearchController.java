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

@RestController
@RequiredArgsConstructor
@RequestMapping("/search")
@Tag(name = "Search")
@Validated
public class SearchController {

    private final SearchQueryService searchQueryService;

    @Operation(summary = "컨텐츠 검색")
    @GetMapping("/contents")
    public ApiResponse<SearchResponseDTO.SearchContentListDTO> searchContent(@AuthenticationPrincipal CustomUserDetails userDetails,
                                        @RequestParam(name = "query") @NotBlank String query) {

        Long userId = userDetails.getUser().getId();
        SearchResponseDTO.SearchContentListDTO response = searchQueryService.searchContent(userId, query);

        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "장소 검색")
    @GetMapping("/places")
    public ApiResponse<SearchResponseDTO.SearchPlaceListDTO> searchPlace(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                               @RequestParam(name = "query") @NotBlank String query) {

        Long userId = userDetails.getUser().getId();
        SearchResponseDTO.SearchPlaceListDTO response = searchQueryService.searchPlace(userId, query);

        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "인기 검색어 조회")
    @GetMapping("/popular")
    public ApiResponse<SearchResponseDTO.GetPopularKeywordDTO> getPopularKeyword() {

        SearchResponseDTO.GetPopularKeywordDTO response = searchQueryService.getPopularKeyword();

        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "TOP 10 촬영지 조회")
    @GetMapping("/top/places")
    public ApiResponse<SearchResponseDTO.SearchPlaceListDTO> getTopPlace(@AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUser().getId();
        SearchResponseDTO.SearchPlaceListDTO response = searchQueryService.getTopPlace(userId);

        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "TOP 10 영화 조회")
    @GetMapping("/top/movie")
    public ApiResponse<SearchResponseDTO.GetTopContentListDTO> getTopMovie(@AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUser().getId();
        SearchResponseDTO.GetTopContentListDTO response = searchQueryService.getTopMovie(userId);

        return ApiResponse.onSuccess(response);
    }
}