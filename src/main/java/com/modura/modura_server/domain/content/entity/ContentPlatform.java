package com.modura.modura_server.domain.content.entity;

import com.modura.modura_server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Table(name = "content_platform", uniqueConstraints = {
        @UniqueConstraint(
                name = "UQ_CONTENT_PLATFORM_ID",
                columnNames = {"content_id", "platform_id"}
        )
})
public class ContentPlatform extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "platform_id", nullable = false)
    private Platform platform;
}
