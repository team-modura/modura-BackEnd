package com.modura.modura_server.domain.user.service;

import com.modura.modura_server.domain.search.dto.SearchResponseDTO;
import com.modura.modura_server.domain.user.dto.UserResponseDTO;

public interface UserQueryService {

    SearchResponseDTO.SearchContentListDTO getLikedContent(Long userId, String type);
    SearchResponseDTO.SearchPlaceListDTO getLikedPlace(Long userId);
    UserResponseDTO.GetMyStillcutListDTO getMyStillcutList(Long userId);
}
