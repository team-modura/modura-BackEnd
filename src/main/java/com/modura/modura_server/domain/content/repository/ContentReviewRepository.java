package com.modura.modura_server.domain.content.repository;

import com.modura.modura_server.domain.content.entity.Content;
import com.modura.modura_server.domain.content.entity.ContentReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ContentReviewRepository extends JpaRepository<ContentReview, Long> {

    @Query(value = "SELECT rating, COUNT(*) FROM content_review WHERE content_id = :contentId GROUP BY rating", nativeQuery = true)
    List<Object[]> findRatingDistribution(@Param("contentId") Long contentId);

    @Query("SELECT COALESCE(ROUND(AVG(r.rating), 1), 0) FROM ContentReview r WHERE r.content.id = :contentId")
    Double findAverageRatingByContentId(@Param("contentId") Long contentId);

    @Query("SELECT cr FROM ContentReview cr JOIN FETCH cr.user WHERE cr.content = :content ORDER BY cr.createdAt DESC")
    List<ContentReview> findByContent(@Param("content") Content content);

    Optional<ContentReview> findByIdAndContent(Long reviewId, Content content);

    @Query("SELECT cr FROM ContentReview cr JOIN FETCH cr.content c WHERE cr.user.id = :userId ORDER BY cr.createdAt DESC")
    List<ContentReview> findAllByUserIdWithContent(@Param("userId") Long userId);

    @Query("SELECT cr FROM ContentReview cr JOIN FETCH cr.content c WHERE cr.user.id = :userId AND c.type = :contentType ORDER BY cr.createdAt DESC")
    List<ContentReview> findAllByUserIdAndContentTypeWithContent(@Param("userId") Long userId, @Param("contentType") Integer contentType);
}
