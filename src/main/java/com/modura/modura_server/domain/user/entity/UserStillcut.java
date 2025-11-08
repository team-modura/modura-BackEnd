package com.modura.modura_server.domain.user.entity;

import com.modura.modura_server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Table(name = "user_stillcut")
public class UserStillcut extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stillcut_id", nullable = false)
    private Stillcut stillcut;

    @Column(name = "image_url", nullable = false, columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "similarity", nullable = false)
    private Integer similarity;

    @Column(name = "angle", nullable = false)
    private Integer angle;

    @Column(name = "clarity", nullable = false)
    private Integer clarity;

    @Column(name = "color", nullable = false)
    private Integer color;

    @Column(name = "palette", nullable = false)
    private Integer palette;
}
