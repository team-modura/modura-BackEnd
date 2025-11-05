package com.modura.modura_server.domain.user.converter;

import com.modura.modura_server.domain.user.dto.UserResponseDTO;
import com.modura.modura_server.domain.user.entity.User;

public class AuthConverter {

    public static UserResponseDTO.GetUserDTO toGetUserDTO(User user, String accessToken, String refreshToken) {

        return UserResponseDTO.GetUserDTO.builder()
                .id(user.getId())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public static UserResponseDTO.KakaoLoginDTO toKakaoLoginDTO(User user, String accessToken, String refreshToken) {

        return UserResponseDTO.KakaoLoginDTO.builder()
                .id(user.getId())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
