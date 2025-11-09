package com.modura.modura_server.domain.place.service;

public interface PlaceCommandService {
    void like(Long placeId, Long userId);
    void unlike(Long placeId, Long userId);
}
