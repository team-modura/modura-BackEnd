package com.modura.modura_server.domain.content.service;

import com.modura.modura_server.domain.content.dto.ContentRequestDTO;
import com.modura.modura_server.domain.content.entity.Content;
import com.modura.modura_server.domain.content.entity.ContentLikes;
import com.modura.modura_server.domain.content.entity.ContentReview;
import com.modura.modura_server.domain.content.repository.*;
import com.modura.modura_server.domain.user.entity.User;
import com.modura.modura_server.domain.user.repository.StillcutRepository;
import com.modura.modura_server.domain.user.repository.UserRepository;
import com.modura.modura_server.domain.user.repository.UserStillcutRepository;
import com.modura.modura_server.global.exception.BusinessException;
import com.modura.modura_server.global.response.code.status.ErrorStatus;
import com.modura.modura_server.global.tmdb.entity.TmdbBlacklist;
import com.modura.modura_server.global.tmdb.repository.TmdbBlacklistRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContentCommandServiceImpl implements ContentCommandService {

    private final ContentRepository contentRepository;
    private final ContentLikesRepository contentLikesRepository;
    private final EntityManager entityManager;
    private final UserRepository userRepository;
    private final ContentReviewRepository contentReviewRepository;
    private final TmdbBlacklistRepository tmdbBlacklistRepository;
    private final StillcutRepository stillcutRepository;
    private final UserStillcutRepository userStillcutRepository;
    private final PlatformRepository platformRepository;
    private final ContentCategoryRepository contentCategoryRepository;

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

    @Override
    @Transactional
    public void postBlacklist(ContentRequestDTO.PostBlacklistDTO request) {

        List<Integer> tmdbIds = request.getTmdbIds();
        if (tmdbIds == null || tmdbIds.isEmpty()) {
            return;
        }

        // 1. 삭제할 Content ID 목록 조회
        List<Content> contentsToDelete = contentRepository.findAllByTmdbIdIn(tmdbIds);
        List<Long> contentIds = contentsToDelete.stream()
                .map(Content::getId)
                .collect(Collectors.toList());

        // 2. 연관된 하위 데이터부터 순차적으로 벌크 삭제
        if (!contentIds.isEmpty()) {
            userStillcutRepository.deleteByContentIdIn(contentIds);
            stillcutRepository.deleteByContentIdIn(contentIds);
            platformRepository.deleteByContentIdIn(contentIds);
            contentCategoryRepository.deleteByContentIdIn(contentIds);
            contentLikesRepository.deleteByContentIdIn(contentIds);
            contentReviewRepository.deleteByContentIdIn(contentIds);
        }

        // 3. Content 삭제
        if (!tmdbIds.isEmpty()) {
            contentRepository.deleteByTmdbIdIn(tmdbIds);
        }

        // 4. TMDB 블랙리스트 테이블에 추가
        Set<Integer> existingBlacklistedIds = tmdbBlacklistRepository.findAllTmdbIds();

        List<TmdbBlacklist> newBlacklistEntries = tmdbIds.stream()
                .filter(id -> !existingBlacklistedIds.contains(id)) // 중복 방지
                .map(id -> TmdbBlacklist.builder().tmdbId(id).build())
                .collect(Collectors.toList());

        if (!newBlacklistEntries.isEmpty()) {
            tmdbBlacklistRepository.saveAll(newBlacklistEntries);
        }
    }
}