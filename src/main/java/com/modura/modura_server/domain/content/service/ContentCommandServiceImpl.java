package com.modura.modura_server.domain.content.service;

import com.modura.modura_server.domain.content.entity.Content;
import com.modura.modura_server.domain.content.entity.ContentLikes;
import com.modura.modura_server.domain.content.repository.ContentLikesRepository;
import com.modura.modura_server.domain.content.repository.ContentRepository;
import com.modura.modura_server.domain.user.entity.User;
import com.modura.modura_server.global.exception.BusinessException;
import com.modura.modura_server.global.response.code.status.ErrorStatus;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContentCommandServiceImpl implements ContentCommandService {
    private final ContentRepository contentRepository;
    private final ContentLikesRepository contentLikesRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public void like(Long contentId, Long userId) {
        if(contentLikesRepository.existsByUserIdAndContentId(userId, contentId)) {
            return;
        }

        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.CONTENT_NOT_FOUND));
        User user = entityManager.getReference(User.class, userId);
        contentLikesRepository.save(
                ContentLikes.builder()
                        .content(content)
                        .user(user)
                        .build()
        );
    }

    @Override
    @Transactional
    public void unlike(Long contentId, Long userId) {
        if(!contentLikesRepository.existsByUserIdAndContentId(userId, contentId)) {
            return;
        }
        contentLikesRepository.deleteByUserIdAndContentId(userId, contentId);
    }
}