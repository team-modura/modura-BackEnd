package com.modura.modura_server.domain.place.service;

import com.modura.modura_server.domain.place.dto.PlaceRequestDTO;
import com.modura.modura_server.domain.place.entity.Place;
import com.modura.modura_server.domain.place.repository.PlaceLikesRepository;
import com.modura.modura_server.domain.place.repository.PlaceRepository;
import com.modura.modura_server.domain.user.entity.Stillcut;
import com.modura.modura_server.domain.user.entity.User;
import com.modura.modura_server.domain.user.entity.UserStillcut;
import com.modura.modura_server.domain.user.repository.StillcutRepository;
import com.modura.modura_server.domain.user.repository.UserRepository;
import com.modura.modura_server.domain.user.repository.UserStillcutRepository;
import com.modura.modura_server.global.exception.BusinessException;
import com.modura.modura_server.global.response.code.status.ErrorStatus;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlaceCommandServiceImpl implements PlaceCommandService {

    private final PlaceRepository placeRepository;
    private final PlaceLikesRepository placeLikesRepository;
    private final EntityManager entityManager;
    private final UserRepository userRepository;
    private final StillcutRepository stillcutRepository;
    private final UserStillcutRepository userStillcutRepository;

    @Override
    @Transactional
    public void like(Long placeId, Long userId) {
        if(placeLikesRepository.existsByUserIdAndPlaceId(userId, placeId)) {
            return;
        }

        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.PLACE_NOT_FOUND));
        User user = entityManager.getReference(User.class, userId);
        placeLikesRepository.save(
                com.modura.modura_server.domain.place.entity.PlaceLikes.builder()
                        .place(place)
                        .user(user)
                        .build()
        );
    }

    @Override
    @Transactional
    public void unlike(Long placeId, Long userId) {
        if(!placeLikesRepository.existsByUserIdAndPlaceId(userId, placeId)) {
            return;
        }
        placeLikesRepository.deleteByUserIdAndPlaceId(userId, placeId);
    }

    @Override
    @Transactional
    public void postStillcut(Long userId, Long placeId, Long stillcutId, PlaceRequestDTO.PostStillcutDTO request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.MEMBER_NOT_FOUND));

        Stillcut stillcut = stillcutRepository.findById(stillcutId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.STILLCUT_NOT_FOUND));

        if (!stillcut.getPlace().getId().equals(placeId)) {
            throw new BusinessException(ErrorStatus.STILLCUT_PLACE_MISMATCH);
        }

        UserStillcut newUserStillcut = UserStillcut.builder()
                .user(user)
                .stillcut(stillcut)
                .imageUrl(request.getImageUrl())
                .similarity(request.getSimilarity())
                .angle(request.getAngle())
                .clarity(request.getClarity())
                .color(request.getColor())
                .palette(request.getPalette())
                .build();

        userStillcutRepository.save(newUserStillcut);
    }
}