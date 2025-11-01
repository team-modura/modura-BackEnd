package com.modura.modura_server.domain.content.entity;

import com.modura.modura_server.domain.user.entity.User;
import com.modura.modura_server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Table(name = "content_likes", uniqueConstraints = {
        @UniqueConstraint(
                name = "UQ_USER_CONTENT_ID",
                columnNames = {"user_id", "content_id"}
        )
})
public class ContentLikes extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;
}
