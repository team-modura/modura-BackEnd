package com.modura.modura_server.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class UserRequestDTO {

    @Getter
    @NoArgsConstructor
    public static class UpdateUserDTO {

        @NotBlank(message = "거주지는 필수입니다.")
        String address;

        @Size(min = 3)
        List<Integer> categoryList;
    }
}
