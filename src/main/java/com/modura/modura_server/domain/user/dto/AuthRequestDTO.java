package com.modura.modura_server.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class AuthRequestDTO {

    @Getter
    @NoArgsConstructor
    public static class CreateUserDTO {

        @NotBlank(message = "닉네임은 필수입니다.")
        String nickname;
    }

    @Getter
    @NoArgsConstructor
    public static class KakaoLoginDTO {

        @NotBlank
        String code;
    }
}
