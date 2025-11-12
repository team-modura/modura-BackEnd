package com.modura.modura_server.domain.user.controller;

import com.modura.modura_server.domain.user.dto.AuthRequestDTO;
import com.modura.modura_server.domain.user.dto.AuthResponseDTO;
import com.modura.modura_server.domain.user.service.AuthCommandService;
import com.modura.modura_server.global.jwt.JwtProvider;
import com.modura.modura_server.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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

    private final AuthCommandService authCommandService;
    private final JwtProvider jwtProvider;

    @Operation(summary = "회원가입(테스트용)")
    @PostMapping("/signup")
    public ApiResponse<AuthResponseDTO.GetUserDTO> createUser(@Valid @RequestBody AuthRequestDTO.CreateUserDTO request) {

        AuthResponseDTO.GetUserDTO response = authCommandService.createUser(request);

        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "카카오 액세스 토큰으로 카카오 로그인",
            description = "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=&redirect_uri=")
    @PostMapping("/login")
    public ApiResponse<AuthResponseDTO.GetUserDTO> kakaoLogin(@Valid @RequestBody AuthRequestDTO.KakaoLoginDTO request) {

        AuthResponseDTO.GetUserDTO response = authCommandService.kakaoLogin(request);

        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "userId로 로그인 (테스트용)")
    @PostMapping("/token")
    @Profile({"dev"})
    public ApiResponse<AuthResponseDTO.GetUserDTO> testLogin(@Valid @RequestBody Long userId) {

        AuthResponseDTO.GetUserDTO response = authCommandService.testLogin(userId);

        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {

        String accessToken = jwtProvider.resolveToken(request);
        authCommandService.logout(accessToken);

        return ApiResponse.onSuccess(null);
    }
}