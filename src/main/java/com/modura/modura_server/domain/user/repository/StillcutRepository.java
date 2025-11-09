package com.modura.modura_server.domain.user.repository;

import com.modura.modura_server.domain.user.entity.Stillcut;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StillcutRepository extends JpaRepository<Stillcut, Long> {
    List<Stillcut> findByContentId(Long contentId);
}
