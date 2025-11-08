package com.modura.modura_server.domain.user.service;

import com.modura.modura_server.domain.content.entity.Category;
import com.modura.modura_server.domain.content.repository.CategoryRepository;
import com.modura.modura_server.domain.user.converter.AuthConverter;
import com.modura.modura_server.domain.user.dto.AuthRequestDTO;
import com.modura.modura_server.domain.user.dto.AuthResponseDTO;
import com.modura.modura_server.domain.user.dto.UserRequestDTO;
import com.modura.modura_server.domain.user.entity.User;
import com.modura.modura_server.domain.user.entity.UserCategory;
import com.modura.modura_server.domain.user.entity.enums.Role;
import com.modura.modura_server.domain.user.repository.UserCategoryRepository;
import com.modura.modura_server.domain.user.repository.UserRepository;
import com.modura.modura_server.global.exception.BusinessException;
import com.modura.modura_server.global.jwt.JwtProvider;
import com.modura.modura_server.global.response.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;
    private final UserCategoryRepository userCategoryRepository;
    private final CategoryRepository categoryRepository;
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
    public AuthResponseDTO.GetUserDTO createUser(AuthRequestDTO.CreateUserDTO request) {

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
    public AuthResponseDTO.GetUserDTO kakaoLogin(AuthRequestDTO.KakaoLoginDTO request) {

        AuthResponseDTO.GetKakaoTokenDTO kakaoToken = getKakaoToken(request.getCode());
        AuthResponseDTO.GetKakaoUserInfoDTO userInfo = getKakaoUserInfo(kakaoToken.getAccessToken());

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

        String accessToken = jwtProvider.generateAccessToken(user);
        String refreshToken = jwtProvider.generateRefreshToken(user);

        return AuthConverter.toGetUserDTO(user, accessToken, refreshToken);
    }

    @Override
    public AuthResponseDTO.GetUserDTO testLogin(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.MEMBER_NOT_FOUND));

        String accessToken = jwtProvider.generateAccessToken(user);
        String refreshToken = jwtProvider.generateRefreshToken(user);

        return AuthConverter.toGetUserDTO(user, accessToken, refreshToken);
    }

    @Override
    public Void updateUser(User user, UserRequestDTO.UpdateUserDTO request) {

        user.updateAddress(request.getAddress());

        if (request.getCategoryList() != null && !request.getCategoryList().isEmpty()) {
            List<UserCategory> newUserCategories = request.getCategoryList().stream()
                    .map(categoryId -> {
                        // Category 엔티티 조회
                        Category category = categoryRepository.findById(categoryId.longValue())
                                .orElseThrow(() -> new BusinessException(ErrorStatus.CATEGORY_NOT_FOUND));
                        // UserCategory 생성
                        return UserCategory.builder()
                                .user(user)
                                .category(category)
                                .build();
                    })
                    .collect(Collectors.toList());

            // JpaRepository.saveAll()을 사용하여 배치 저장
            userCategoryRepository.saveAll(newUserCategories);
        }

        return null;
    }

    private AuthResponseDTO.GetKakaoTokenDTO getKakaoToken(String code) {

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", CLIENT_ID);
        formData.add("redirect_uri", REDIRECT_URI);
        formData.add("code", code);

        return webClient.post()
                .uri(KAKAO_TOKEN_URI)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(AuthResponseDTO.GetKakaoTokenDTO.class)
                .block();
    }

    private AuthResponseDTO.GetKakaoUserInfoDTO getKakaoUserInfo(String accessToken) {

        return webClient.get()
                .uri(KAKAO_USER_INFO_URI)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(AuthResponseDTO.GetKakaoUserInfoDTO.class)
                .block();
    }
}