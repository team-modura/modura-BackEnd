package com.modura.modura_server.domain.place.service;

import com.modura.modura_server.domain.place.dto.PlaceResponseDTO;
import com.modura.modura_server.domain.user.converter.UserConverter;
import com.modura.modura_server.domain.user.entity.Stillcut;
import com.modura.modura_server.domain.user.repository.StillcutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaceQueryServiceImpl implements PlaceQueryService {

    private final StillcutRepository stillcutRepository;

    @Override
    @Transactional(readOnly = true)
    public PlaceResponseDTO.GetStillcutListDTO getStillcut(Long placeId) {

        List<Stillcut> stillcutList = stillcutRepository.findByPlaceIdWithContent(placeId);

        return UserConverter.toGetStillcutListDTO(stillcutList);
    }
}