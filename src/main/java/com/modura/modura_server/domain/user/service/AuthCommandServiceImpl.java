package com.modura.modura_server.domain.user.service;

import com.modura.modura_server.domain.user.converter.AuthConverter;
import com.modura.modura_server.domain.user.dto.AuthRequestDTO;
import com.modura.modura_server.domain.user.dto.AuthResponseDTO;
import com.modura.modura_server.domain.user.entity.User;
import com.modura.modura_server.domain.user.entity.enums.Role;
import com.modura.modura_server.domain.user.repository.UserRepository;
import com.modura.modura_server.global.exception.BusinessException;
import com.modura.modura_server.global.jwt.JwtProvider;
import com.modura.modura_server.global.response.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthCommandServiceImpl implements AuthCommandService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final WebClient webClient;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${kakao.client-id}")
    private String CLIENT_ID;

    @Value("${kakao.redirect-uri}")
    private String REDIRECT_URI;

    private static final String KAKAO_USER_INFO_URI = "https://kapi.kakao.com/v2/user/me";

    @Override
    @Transactional
    public AuthResponseDTO.GetUserDTO createUser(AuthRequestDTO.CreateUserDTO request) {

        User user = User.builder()
                .nickname(request.getNickname())
                .role(Role.ROLE_USER)
                .build();
        userRepository.save(user);

        String accessToken = jwtProvider.generateAccessToken(user);
        String refreshToken = jwtProvider.generateRefreshToken(user);

        return AuthConverter.toGetUserDTO(user, accessToken, refreshToken, true);
    }

    @Override
    @Transactional
    public AuthResponseDTO.GetUserDTO kakaoLogin(AuthRequestDTO.KakaoLoginDTO request) {

        AuthResponseDTO.GetKakaoUserInfoDTO userInfo = getKakaoUserInfo(request.getAccessToken());

        String nickname =
                userInfo.getKakaoAccount() != null
                        && userInfo.getKakaoAccount().getProfile() != null
                        && userInfo.getKakaoAccount().getProfile().getNickname() != null
                        ? userInfo.getKakaoAccount().getProfile().getNickname()
                        : "카카오 사용자_" + userInfo.getId();

        User user = userRepository.findByOauthId(userInfo.getId())
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .oauthId(userInfo.getId())
                            .nickname(nickname)
                            .role(Role.ROLE_USER)
                            .build();
                    return userRepository.save(newUser);
                });

        boolean isNewUser = (user.getAddress() == null);
        String accessToken = jwtProvider.generateAccessToken(user);
        String refreshToken = jwtProvider.generateRefreshToken(user);

        return AuthConverter.toGetUserDTO(user, accessToken, refreshToken, isNewUser);
    }

    @Override
    @Transactional
    public AuthResponseDTO.GetUserDTO testLogin(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.MEMBER_NOT_FOUND));

        boolean isNewUser = (user.getAddress() == null);
        String accessToken = jwtProvider.generateAccessToken(user);
        String refreshToken = jwtProvider.generateRefreshToken(user);

        return AuthConverter.toGetUserDTO(user, accessToken, refreshToken, isNewUser);
    }

    @Override
    @Transactional
    public void logout(String accessToken) {

        if (!jwtProvider.validateToken(accessToken)) {
            throw new BusinessException(ErrorStatus.UNAUTHORIZED);
        }

        Long remainingTime = jwtProvider.getRemainingExpiration(accessToken);

        // Redis에 (Key: 토큰, Value: "logout", TTL: 남은 유효 시간) 저장
        if (remainingTime > 0) {
            redisTemplate.opsForValue().set(
                    accessToken,
                    "logout",
                    remainingTime,
                    TimeUnit.MILLISECONDS
            );
        }
    }

    private AuthResponseDTO.GetKakaoUserInfoDTO getKakaoUserInfo(String accessToken) {

        return webClient.get()
                .uri(KAKAO_USER_INFO_URI)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(AuthResponseDTO.GetKakaoUserInfoDTO.class)
                .block(Duration.ofSeconds(10));
    }
}