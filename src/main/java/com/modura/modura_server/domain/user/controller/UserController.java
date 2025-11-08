package com.modura.modura_server.domain.user.controller;

import com.modura.modura_server.domain.user.dto.AuthResponseDTO;
import com.modura.modura_server.domain.user.dto.UserRequestDTO;
import com.modura.modura_server.domain.user.entity.User;
import com.modura.modura_server.domain.user.service.UserCommandService;
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
@RequestMapping("/users")
@Tag(name = "User")
@Validated
public class UserController {

    private final UserCommandService userCommandService;

    @Operation(summary = "유저 정보 등록")
    @PatchMapping
    public ApiResponse<Void> updateUser(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                              @Valid @RequestBody UserRequestDTO.UpdateUserDTO request) {

        User user = userDetails.getUser();
        Void response = userCommandService.updateUser(user, request);

        return ApiResponse.onSuccess(response);
    }
}