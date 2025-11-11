package com.modura.modura_server.domain.user.controller;

import com.modura.modura_server.domain.search.dto.SearchResponseDTO;
import com.modura.modura_server.domain.user.dto.UserRequestDTO;
import com.modura.modura_server.domain.user.service.UserCommandService;
import com.modura.modura_server.domain.user.service.UserQueryService;
import com.modura.modura_server.global.response.ApiResponse;
import com.modura.modura_server.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "User")
@Validated
public class UserController {

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;

    @Operation(summary = "유저 정보 등록")
    @PatchMapping
    public ApiResponse<Void> updateUser(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                              @Valid @RequestBody UserRequestDTO.UpdateUserDTO request) {

        Long userId = userDetails.getUser().getId();
        Void response = userCommandService.updateUser(userId, request);

        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "찜한 컨텐츠 조회")
    @GetMapping("/likes/contents")
    public ApiResponse<SearchResponseDTO.SearchContentListDTO> getLikedContent(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                              @RequestParam(name = "type") @NotBlank String type) {

        Long userId = userDetails.getUser().getId();
        SearchResponseDTO.SearchContentListDTO response = userQueryService.getLikedContent(userId, type);

        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "찜한 촬영지 조회")
    @GetMapping("/likes/places")
    public ApiResponse<SearchResponseDTO.SearchPlaceListDTO> getLikedPlace(@AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUser().getId();
        SearchResponseDTO.SearchPlaceListDTO response = userQueryService.getLikedPlace(userId);

        return ApiResponse.onSuccess(response);
    }
}