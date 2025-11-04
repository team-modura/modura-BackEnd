package com.modura.modura_server.domain.user.converter;

import com.modura.modura_server.domain.user.dto.UserResponseDTO;
import com.modura.modura_server.domain.user.entity.User;

public class AuthConverter {

    public static UserResponseDTO.GetUserDTO toUser(User user, String accessToken, String refreshToken) {

        return UserResponseDTO.GetUserDTO.builder()
                .id(user.getId())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
