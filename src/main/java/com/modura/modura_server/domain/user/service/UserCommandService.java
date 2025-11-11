package com.modura.modura_server.domain.user.service;

import com.modura.modura_server.domain.user.dto.UserRequestDTO;

public interface UserCommandService {
    Void updateUser(Long userId, UserRequestDTO.UpdateUserDTO request);
}
