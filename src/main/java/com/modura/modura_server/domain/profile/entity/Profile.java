package com.modura.modura_server.domain.profile.entity;

import com.modura.modura_server.domain.announcement.entity.Likes;
import com.modura.modura_server.domain.profile.entity.mapping.UserHouseHold;
import com.modura.modura_server.domain.user.entity.User;
import com.modura.modura_server.global.enums.Assets;
import com.modura.modura_server.global.enums.Car;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Table(name = "Profiles")
public class Profile {
    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserHouseHold> userHouseHoldList = new ArrayList<>();

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "address_detail")
    private String addressDetail;

    @Column(name = "income_decile", nullable = false)
    private Integer incomeDecile;

    @Enumerated(EnumType.STRING)
    @Column(name = "car", nullable = false)
    private Car car;

    @Enumerated(EnumType.STRING)
    @Column(name = "assets", nullable = false)
    private Assets assets;

    @Column(name = "occupation", nullable = false)
    private String occupation;
}