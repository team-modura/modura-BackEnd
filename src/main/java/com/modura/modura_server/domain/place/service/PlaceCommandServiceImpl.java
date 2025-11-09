package com.modura.modura_server.domain.place.service;

import com.modura.modura_server.domain.place.entity.Place;
import com.modura.modura_server.domain.place.repository.PlaceLikesRepository;
import com.modura.modura_server.domain.place.repository.PlaceRepository;
import com.modura.modura_server.domain.user.entity.User;
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
}