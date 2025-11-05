package com.modura.modura_server.domain.user.controller;

import com.modura.modura_server.domain.user.dto.AuthRequestDTO;
import com.modura.modura_server.domain.user.dto.AuthResponseDTO;
import com.modura.modura_server.domain.user.service.UserCommandService;
import com.modura.modura_server.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
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
    public ApiResponse<AuthResponseDTO.GetUserDTO> createUser(@Valid @RequestBody AuthRequestDTO.CreateUserDTO request) {

        AuthResponseDTO.GetUserDTO response = userCommandService.createUser(request);

        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "인가 코드로 카카오 로그인",
            description = "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=${REST_API_KEY}&redirect_uri=http://localhost:8080/redirect")
    @PostMapping("/login")
    public ApiResponse<AuthResponseDTO.GetUserDTO> kakaoLogin(@Valid @RequestBody AuthRequestDTO.KakaoLoginDTO request) {

        AuthResponseDTO.GetUserDTO response = userCommandService.kakaoLogin(request);

        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "userId로 로그인 (테스트용)")
    @PostMapping("/token")
    @Profile({"dev"})
    public ApiResponse<AuthResponseDTO.GetUserDTO> testLogin(@Valid @RequestBody Long userId) {

        AuthResponseDTO.GetUserDTO response = userCommandService.testLogin(userId);

        return ApiResponse.onSuccess(response);
    }
}