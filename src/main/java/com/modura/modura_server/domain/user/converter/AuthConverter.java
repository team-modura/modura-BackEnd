package com.modura.modura_server.domain.user.converter;

import com.modura.modura_server.domain.user.dto.AuthResponseDTO;
import com.modura.modura_server.domain.user.entity.User;

public class AuthConverter {

    public static AuthResponseDTO.GetUserDTO toGetUserDTO(User user, String accessToken, String refreshToken, Boolean isNewUser) {

        return AuthResponseDTO.GetUserDTO.builder()
                .id(user.getId())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .isNewUser(isNewUser)
                .username(user.getNickname())
                .isInactive(user.isInactive())
                .build();
    }
}
