package com.modura.modura_server.domain.user.service;

import com.modura.modura_server.domain.user.dto.UserRequestDTO;

public interface UserCommandService {

    void updateUser(Long userId, UserRequestDTO.UpdateUserDTO request);
}
