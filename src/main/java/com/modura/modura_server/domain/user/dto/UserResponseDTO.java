package com.modura.modura_server.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class UserResponseDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetMyStillcutDTO {

        Long id;
        String imageUrl;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetMyStillcutListDTO {

        List<GetMyStillcutDTO> stillcutList;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetMyStillcutDetailDTO {

        Long id;
        String imageUrl;
        String stillcut;
        String title;
        String name;
        String date;
    }
}
