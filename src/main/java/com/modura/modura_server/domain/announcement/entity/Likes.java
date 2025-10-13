package com.modura.modura_server.domain.announcement.entity;

import com.modura.modura_server.domain.user.entity.User;
import com.modura.modura_server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Table(name = "likes", uniqueConstraints = {
        @UniqueConstraint(
                name = "UQ_USER_ANNOUNCEMENT_ID",
                columnNames = {"user_id", "announcement_id"}
        )
})
public class Likes extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "announcement_id", nullable = false)
    private Announcement announcement;
}