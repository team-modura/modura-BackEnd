package com.modura.modura_server.domain.place.service;

import com.modura.modura_server.domain.place.dto.PlaceResponseDTO;

public interface PlaceQueryService {

    PlaceResponseDTO.GetStillcutListDTO getStillcut(Long placeId);
}
