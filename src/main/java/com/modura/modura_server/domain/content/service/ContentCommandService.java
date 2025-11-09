package com.modura.modura_server.domain.content.service;

import com.modura.modura_server.domain.content.dto.ContentRequestDTO;
import com.modura.modura_server.domain.content.dto.ContentResponseDTO;

public interface ContentCommandService {
    void like(Long contentId, Long userId);
    void unlike(Long contentId, Long userId);
    void postContentReview(Long contentId, Long userId, ContentRequestDTO.ReviewReqDTO reviewReqDTO);
    void patchContentReview(Long contentId, Long reviewId,Long userId, ContentRequestDTO.ReviewUpdateReqDTO reviewReqDTO);
    void deleteContentReview(Long contentId, Long reviewId, Long userId);
}
