package com.modura.modura_server.domain.user.service;

import com.modura.modura_server.domain.user.dto.UserRequestDTO;
import com.modura.modura_server.domain.user.entity.User;

public interface UserCommandService {

    Void updateUser(User user, UserRequestDTO.UpdateUserDTO request);
}
