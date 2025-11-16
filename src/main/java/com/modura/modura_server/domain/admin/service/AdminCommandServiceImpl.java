package com.modura.modura_server.domain.admin.service;

import com.modura.modura_server.domain.admin.dto.AdminRequestDTO;
import com.modura.modura_server.domain.content.entity.Content;
import com.modura.modura_server.domain.content.repository.ContentRepository;
import com.modura.modura_server.domain.place.entity.Place;
import com.modura.modura_server.domain.place.repository.PlaceRepository;
import com.modura.modura_server.domain.user.entity.Stillcut;
import com.modura.modura_server.domain.user.repository.StillcutRepository;
import com.modura.modura_server.global.exception.BusinessException;
import com.modura.modura_server.global.response.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminCommandServiceImpl implements AdminCommandService {

    private final PlaceRepository placeRepository;
    private final ContentRepository contentRepository;
    private final StillcutRepository stillcutRepository;

    @Override
    public void createPlace(AdminRequestDTO.CreatePlaceDTO request) {

        Place newPlace = Place.builder()
                .name(request.getName())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .thumbnail(request.getThumbnail())
                .build();

        placeRepository.save(newPlace);
    }

    @Override
    public void createStillcut(AdminRequestDTO.CreateStillcutDTO request) {

        // 1. Content 엔티티 조회
        Content content = contentRepository.findById(request.getContentId())
                .orElseThrow(() -> new BusinessException(ErrorStatus.CONTENT_NOT_FOUND));

        // 2. Place 엔티티 조회
        Place place = placeRepository.findById(request.getPlaceId())
                .orElseThrow(() -> new BusinessException(ErrorStatus.PLACE_NOT_FOUND));

        // 3. Stillcut 생성 및 저장
        Stillcut newStillcut = Stillcut.builder()
                .content(content)
                .place(place)
                .imageUrl(request.getImageUrl())
                .build();

        stillcutRepository.save(newStillcut);
    }
}