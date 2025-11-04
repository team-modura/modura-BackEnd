package com.modura.modura_server.domain.user.controller;

import com.modura.modura_server.domain.user.dto.UserRequestDTO;
import com.modura.modura_server.domain.user.dto.UserResponseDTO;
import com.modura.modura_server.domain.user.service.UserCommandService;
import com.modura.modura_server.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Auth")
@Validated
public class AuthController {

    private final UserCommandService userCommandService;

    @Operation(summary = "회원가입(테스트용)")
    @PostMapping("/signup")
    public ApiResponse<UserResponseDTO.GetUserDTO> createUser(@Valid @RequestBody UserRequestDTO.CreateUserDTO request) {

        UserResponseDTO.GetUserDTO response = userCommandService.createUser(request);

        return ApiResponse.onSuccess(response);
    }
}