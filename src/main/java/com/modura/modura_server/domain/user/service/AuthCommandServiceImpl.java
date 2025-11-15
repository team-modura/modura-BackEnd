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
    private final RedisTemplate<String, String> redisTemplate;

    private static final String KAKAO_USER_INFO_URI = "https://kapi.kakao.com/v2/user/me";
    private static final String REFRESH_TOKEN_PREFIX = "RT:";
    private static final String BLACKLIST_VALUE = "logout";

    @Override
    @Transactional
    public AuthResponseDTO.GetUserDTO createUser(AuthRequestDTO.CreateUserDTO request) {

        User user = User.builder()
                .nickname(request.getNickname())
                .role(Role.ROLE_USER)
                .build();
        userRepository.save(user);

        return generateAndSaveTokens(user, true);
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

        return generateAndSaveTokens(user, isNewUser);
    }

    @Override
    @Transactional
    public AuthResponseDTO.GetUserDTO testLogin(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.MEMBER_NOT_FOUND));

        boolean isNewUser = (user.getAddress() == null);

        return generateAndSaveTokens(user, isNewUser);
    }

    @Override
    @Transactional
    public void logout(String accessToken) {

        String userId = getUserIdFromToken(accessToken);

        redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
        blacklistAccessToken(accessToken);
    }

    @Override
    @Transactional
    public AuthResponseDTO.GetUserDTO reissueToken(String accessToken, String refreshToken) {

        String userId = getUserIdFromToken(accessToken);
        validateRefreshToken(userId, refreshToken);

        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new BusinessException(ErrorStatus.MEMBER_NOT_FOUND));

        boolean isNewUser = (user.getAddress() == null);

        return generateAndSaveTokens(user, isNewUser);
    }

    @Override
    @Transactional
    public void withdrawal(Long userId, String accessToken) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.MEMBER_NOT_FOUND));

        // 이미 탈퇴 처리된 경우, 로그아웃만 실행
        if (user.isInactive()) {
            logout(accessToken);
            return;
        }

        user.deactivate();
        logout(accessToken);
    }

    @Override
    @Transactional
    public void reactivate(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.MEMBER_NOT_FOUND));

        if (!user.isInactive()) {
            return;
        }

        user.reactivate();
    }

    private AuthResponseDTO.GetUserDTO generateAndSaveTokens(User user, boolean isNewUser) {

        String accessToken = jwtProvider.generateAccessToken(user);
        String refreshToken = jwtProvider.generateRefreshToken(user);

        saveRefreshTokenToRedis(user.getId().toString(), refreshToken);

        return AuthConverter.toGetUserDTO(user, accessToken, refreshToken, isNewUser);
    }

    /**
     * Refresh Token을 Redis에 저장
     */
    private void saveRefreshTokenToRedis(String userId, String refreshToken) {

        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + userId,
                refreshToken,
                JwtProvider.REFRESH_TOKEN_VALIDITY_MS,
                TimeUnit.MILLISECONDS
        );
    }

    private AuthResponseDTO.GetKakaoUserInfoDTO getKakaoUserInfo(String accessToken) {

        return webClient.get()
                .uri(KAKAO_USER_INFO_URI)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(AuthResponseDTO.GetKakaoUserInfoDTO.class)
                .block(Duration.ofSeconds(10));
    }

    private String getUserIdFromToken(String token) {
        try {
            return jwtProvider.getUserIdFromToken(token);
        } catch (Exception e) {
            throw new BusinessException(ErrorStatus.INVALID_TOKEN);
        }
    }

    private void blacklistAccessToken(String accessToken) {

        Long remainingTime = jwtProvider.getRemainingExpiration(accessToken);
        if (remainingTime > 0) {
            redisTemplate.opsForValue().set(
                    accessToken,
                    BLACKLIST_VALUE,
                    remainingTime,
                    TimeUnit.MILLISECONDS
            );
        }
    }

    private void validateRefreshToken(String userId, String clientRefreshToken) {

        String storedRefreshToken = redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + userId);

        if (storedRefreshToken == null) {
            throw new BusinessException(ErrorStatus.REFRESH_TOKEN_NOT_FOUND);
        }

        if (!storedRefreshToken.equals(clientRefreshToken)) {
            // 토큰 불일치 시 탈취로 간주하고 즉시 삭제
            redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
            throw new BusinessException(ErrorStatus.REFRESH_TOKEN_MISMATCH);
        }

        // RT 자체의 만료 시간 검증
        if (!jwtProvider.validateToken(clientRefreshToken)) {
            throw new BusinessException(ErrorStatus.REFRESH_TOKEN_EXPIRED);
        }
    }
}