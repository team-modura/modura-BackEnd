package com.modura.modura_server.domain.user.entity;

import com.modura.modura_server.global.entity.BaseEntity;
import com.modura.modura_server.domain.user.entity.enums.Gender;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "inactive_date")
    private LocalDate inactiveDate;

    @Column(name = "oauth_id")
    private String oauthId;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Column(name = "birth", nullable = false)
    private String birth;

    @Column(name = "phone", nullable = false, length = 13)
    private String phone;

    @Column(name = "image", columnDefinition = "TEXT")
    private String image;
}