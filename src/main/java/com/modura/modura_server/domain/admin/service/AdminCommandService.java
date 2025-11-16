package com.modura.modura_server.domain.admin.service;

import com.modura.modura_server.domain.admin.dto.AdminRequestDTO;

public interface AdminCommandService {

    void createPlace(AdminRequestDTO.CreatePlaceDTO request);
    void createStillcut(AdminRequestDTO.CreateStillcutDTO request);
}
