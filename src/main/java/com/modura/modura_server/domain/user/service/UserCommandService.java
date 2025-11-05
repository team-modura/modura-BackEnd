package com.modura.modura_server.domain.user.service;

import com.modura.modura_server.domain.user.dto.UserRequestDTO;
import com.modura.modura_server.domain.user.dto.UserResponseDTO;

public interface UserCommandService {

    UserResponseDTO.GetUserDTO createUser(UserRequestDTO.CreateUserDTO request);
    UserResponseDTO.LoginDTO kakaoLogin(UserRequestDTO.KakaoLoginDTO request);
    UserResponseDTO.LoginDTO testLogin(Long userId);
}
