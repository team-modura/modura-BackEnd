package com.modura.modura_server.domain.user.entity;

import com.modura.modura_server.domain.announcement.entity.Likes;
import com.modura.modura_server.domain.profile.entity.Profile;
import com.modura.modura_server.global.entity.BaseEntity;
import com.modura.modura_server.global.enums.Gender;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "gender", nullable = false)
    private Gender gender;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "birth", nullable = false)
    private String birth;

    @Column(name = "phone", nullable = false, length = 13)
    private String phone;

    @Column(name = "image", columnDefinition = "TEXT")
    private String image;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "id")
    private Profile userProfile;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Likes> likesList = new ArrayList<>();
}