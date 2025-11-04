package com.modura.modura_server.domain.user.repository;

import com.modura.modura_server.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findById(Long id);
    Optional<User> findByOauthId(String oauthId);
    Boolean existsByOauthId(String oauthId);
}
