package com.modura.modura_server.domain.content.service;

import com.modura.modura_server.domain.content.dto.ContentResponseDTO;

public interface ContentQueryService {
    ContentResponseDTO.ContentDetailDTO getContentDetail(Long contentId, Long userId);
    ContentResponseDTO.ReviewItemDTO getContentReviewItem(Long contentId, Long reviewId);
    ContentResponseDTO.ReviewListDTO getContentReviewList(Long contentId);
}
