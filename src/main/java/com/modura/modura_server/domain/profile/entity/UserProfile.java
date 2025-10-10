package com.modura.modura_server.domain.profile.entity;

import com.modura.modura_server.domain.user.entity.User;
import com.modura.modura_server.global.entity.BaseEntity;
import com.modura.modura_server.domain.profile.entity.enums.Assets;
import com.modura.modura_server.domain.profile.entity.enums.Car;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Table(name = "user_profile")
public class UserProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

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