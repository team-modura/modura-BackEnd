package com.modura.modura_server.domain.user.service;

import com.modura.modura_server.domain.user.converter.AuthConverter;
import com.modura.modura_server.domain.user.dto.UserRequestDTO;
import com.modura.modura_server.domain.user.dto.UserResponseDTO;
import com.modura.modura_server.domain.user.entity.User;
import com.modura.modura_server.domain.user.entity.enums.Role;
import com.modura.modura_server.domain.user.repository.UserRepository;
import com.modura.modura_server.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;
@Slf4j
@Service
@RequiredArgsConstructor
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final WebClient webClient;

    @Value("${kakao.client-id}")
    private String CLIENT_ID;

    @Value("${kakao.redirect-uri}")
    private String REDIRECT_URI;

    private static final String KAKAO_TOKEN_URI = "https://kauth.kakao.com/oauth/token";
    private static final String KAKAO_USER_INFO_URI = "https://kapi.kakao.com/v2/user/me";

    @Override
    @Transactional
    public UserResponseDTO.GetUserDTO createUser(UserRequestDTO.CreateUserDTO request) {

        User user = User.builder()
                .nickname(request.getNickname())
                .role(Role.ROLE_USER)
                .build();
        userRepository.save(user);

        String accessToken = jwtProvider.generateAccessToken(user);
        String refreshToken = jwtProvider.generateRefreshToken(user);

        return AuthConverter.toGetUserDTO(user, accessToken, refreshToken);
    }

    @Override
    @Transactional
    public UserResponseDTO.KakaoLoginDTO kakaoLogin(UserRequestDTO.KakaoLoginDTO request) {

        UserResponseDTO.GetKakaoTokenDTO kakaoToken = getKakaoToken(request.getCode());
        log.info("\n\nkakaoToken: {}", kakaoToken);
        UserResponseDTO.GetKakaoUserInfoDTO userInfo = getKakaoUserInfo(kakaoToken.getAccessToken());

        User user = userRepository.findByOauthId(userInfo.getId())
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .oauthId(userInfo.getId())
                            .nickname(userInfo.getKakaoAccount().getProfile().getNickname())
                            .role(Role.ROLE_USER)
                            .build();
                    return userRepository.save(newUser);
                });

        String accessToken = jwtProvider.generateAccessToken(user);
        String refreshToken = jwtProvider.generateRefreshToken(user);


        return AuthConverter.toKakaoLoginDTO(user, accessToken, refreshToken);
    }

    private UserResponseDTO.GetKakaoTokenDTO getKakaoToken(String code) {

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", CLIENT_ID);
        formData.add("redirect_uri", REDIRECT_URI);
        formData.add("code", code);
        log.info("\n\ngetKakaoToken code: {}", code);
        return webClient.post()
                .uri(KAKAO_TOKEN_URI)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(UserResponseDTO.GetKakaoTokenDTO.class)
                .block();
    }

    private UserResponseDTO.GetKakaoUserInfoDTO getKakaoUserInfo(String accessToken) {
        log.info("\n\ngetKakaoUserInfo: {}", accessToken);
        return webClient.get()
                .uri(KAKAO_USER_INFO_URI)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(UserResponseDTO.GetKakaoUserInfoDTO.class)
                .block();
    }
}