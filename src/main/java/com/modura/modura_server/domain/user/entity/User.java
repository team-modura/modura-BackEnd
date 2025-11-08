package com.modura.modura_server.domain.user.entity;

import com.modura.modura_server.global.entity.BaseEntity;
import com.modura.modura_server.domain.user.entity.enums.Role;
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
    private Long oauthId;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "address")
    private String address;

    public void updateAddress(String address) {
        this.address = address;
    }
}