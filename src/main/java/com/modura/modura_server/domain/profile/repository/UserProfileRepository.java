package com.modura.modura_server.domain.profile.repository;

import com.modura.modura_server.domain.profile.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

}
