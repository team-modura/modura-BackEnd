package com.modura.modura_server.domain.content.service;

import com.modura.modura_server.domain.content.dto.ContentResponseDTO;

public interface ContentQueryService {
    ContentResponseDTO.ContentDetailDTO getContentDetail(Long contentId, Long userId);
}
