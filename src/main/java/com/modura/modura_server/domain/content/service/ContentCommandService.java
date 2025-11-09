package com.modura.modura_server.domain.content.service;

public interface ContentCommandService {
    void like(Long contentId, Long userId);
    void unlike(Long contentId, Long userId);
}
