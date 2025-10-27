package com.modura.modura_server.domain.user.converter;

import com.modura.modura_server.domain.user.dto.UserResponseDTO;
import com.modura.modura_server.domain.user.entity.Terms;

public class UserConverter {

    public static UserResponseDTO.GetTermsDTO toGetTermsDTO(Terms terms) {

        return UserResponseDTO.GetTermsDTO.builder()
                .title(terms.getTitle())
                .body(terms.getBody())
                .optional(terms.getOptional())
                .build();
    }
}
