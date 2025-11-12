package com.modura.modura_server.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class AuthResponseDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetUserDTO {

        Long id;
        String accessToken;
        String refreshToken;
        Boolean isNewUser;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetKakaoUserInfoDTO {

        Long id;

        @JsonProperty("kakao_account")
        private AuthResponseDTO.GetKakaoUserInfoDTO.KakaoAccount kakaoAccount;

        @Getter
        @NoArgsConstructor
        public static class KakaoAccount {
            private AuthResponseDTO.GetKakaoUserInfoDTO.KakaoAccount.Profile profile;

            @Getter
            @NoArgsConstructor
            public static class Profile {
                private String nickname;
            }
        }
    }
}
