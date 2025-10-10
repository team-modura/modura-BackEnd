package com.modura.modura_server.domain.announcement.repository;

import com.modura.modura_server.domain.announcement.entity.Likes;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikesRepository extends JpaRepository<Likes, Long> {

}
