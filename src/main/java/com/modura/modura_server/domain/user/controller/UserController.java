package com.modura.modura_server.domain.user.controller;

import com.modura.modura_server.domain.user.dto.UserResponseDTO;
import com.modura.modura_server.domain.user.service.UserQueryService;
import com.modura.modura_server.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "User")
@Validated
public class UserController {

    private final UserQueryService userQueryService;

    @Operation(summary = "이용약관 조회")
    @GetMapping("/terms/{termsId}")
    public ApiResponse<UserResponseDTO.GetTermsDTO> getTeamGallery(@PathVariable Long termsId) {

        UserResponseDTO.GetTermsDTO response = userQueryService.getTerms(termsId);

        return ApiResponse.onSuccess(response);
    }
}