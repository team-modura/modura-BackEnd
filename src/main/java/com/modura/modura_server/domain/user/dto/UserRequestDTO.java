package com.modura.modura_server.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class UserRequestDTO {

    @Builder
    @AllArgsConstructor
    @Getter
    @NoArgsConstructor
    public static class UpdateUserDTO {

        @NotBlank(message = "거주지는 필수입니다.")
        String address;

    @NotNull(message = "카테고리는 필수입니다.")
    @Size(min = 3, message = "카테고리는 최소 3개 이상 선택해야 합니다.")
        List<Integer> categoryList;
    }
}
