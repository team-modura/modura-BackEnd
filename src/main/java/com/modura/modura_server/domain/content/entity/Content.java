package com.modura.modura_server.domain.content.entity;

import com.modura.modura_server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Table(name = "content")
public class Content extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title_kr", nullable = false)
    private String titleKr;

    @Column(name = "title_eng", nullable = false)
    private String titleEng;

    @Column(name = "year")
    private Integer year;

    @Column(name = "plot")
    private String plot;

    @Column(name = "thumbnail", columnDefinition = "TEXT")
    private String thumbnail;
}
