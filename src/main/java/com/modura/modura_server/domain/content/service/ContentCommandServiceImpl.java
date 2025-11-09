package com.modura.modura_server.domain.content.service;

import com.modura.modura_server.domain.content.dto.ContentRequestDTO;
import com.modura.modura_server.domain.content.entity.Content;
import com.modura.modura_server.domain.content.entity.ContentLikes;
import com.modura.modura_server.domain.content.entity.ContentReview;
import com.modura.modura_server.domain.content.repository.ContentLikesRepository;
import com.modura.modura_server.domain.content.repository.ContentRepository;
import com.modura.modura_server.domain.content.repository.ContentReviewRepository;
import com.modura.modura_server.domain.user.entity.User;
import com.modura.modura_server.domain.user.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final ContentReviewRepository contentReviewRepository;

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

    @Override
    @Transactional
    public void postContentReview(Long contentId, Long userId, ContentRequestDTO.ReviewReqDTO reviewReqDTO) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.CONTENT_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.MEMBER_NOT_FOUND));

        ContentReview review = ContentReview.builder()
                .content(content)
                .user(user)
                .rating(reviewReqDTO.getRating())
                .body(reviewReqDTO.getComment())
                .build();
        contentReviewRepository.save(review);
    }

    @Override
    @Transactional
    public void patchContentReview(Long contentId, Long reviewId, Long userId, ContentRequestDTO.ReviewUpdateReqDTO reviewReqDTO) {
                ContentReview review = contentReviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.CONTENT_REVIEW_NOT_FOUND));

        if(!review.getUser().getId().equals(userId) || !review.getContent().getId().equals(contentId)) {
            throw new BusinessException(ErrorStatus.FORBIDDEN);
        }

        Integer newRating = reviewReqDTO.getRating();
        String newComment = reviewReqDTO.getComment();

        if (newRating != null) {
            review.setRating(newRating);
        }

        if (newComment != null) {
            review.setBody(newComment);
        }

        if (newRating == null && newComment == null) {
            throw new BusinessException(ErrorStatus.BAD_REQUEST);
        }
        contentReviewRepository.save(review);
    }

    @Override
    @Transactional
    public void deleteContentReview(Long contentId, Long reviewId, Long userId) {
        ContentReview review = contentReviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.CONTENT_REVIEW_NOT_FOUND));

        if(!review.getUser().getId().equals(userId) || !review.getContent().getId().equals(contentId)) {
            throw new BusinessException(ErrorStatus.FORBIDDEN);
        }

        contentReviewRepository.delete(review);
    }
}