package com.modura.modura_server.domain.profile.entity.mapping;

import com.modura.modura_server.domain.profile.entity.Profile;
import com.modura.modura_server.domain.profile.entity.RealEstate;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Table(name = "user_real_estate")
public class UserRealEstate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Profile profile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "real_estate_id", nullable = false)
    private RealEstate realEstate;
}
