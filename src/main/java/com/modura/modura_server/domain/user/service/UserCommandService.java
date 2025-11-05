package com.modura.modura_server.domain.user.service;

import com.modura.modura_server.domain.user.dto.AuthRequestDTO;
import com.modura.modura_server.domain.user.dto.AuthResponseDTO;

public interface UserCommandService {

    AuthResponseDTO.GetUserDTO createUser(AuthRequestDTO.CreateUserDTO request);
    AuthResponseDTO.GetUserDTO kakaoLogin(AuthRequestDTO.KakaoLoginDTO request);
    AuthResponseDTO.GetUserDTO testLogin(Long userId);
}
