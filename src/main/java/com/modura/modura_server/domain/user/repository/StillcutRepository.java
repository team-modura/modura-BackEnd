package com.modura.modura_server.domain.user.repository;

import com.modura.modura_server.domain.content.entity.Content;
import com.modura.modura_server.domain.user.entity.Stillcut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StillcutRepository extends JpaRepository<Stillcut, Long> {

    @Query("SELECT s FROM Stillcut s JOIN FETCH s.place WHERE s.content IN :contents")
    List<Stillcut> findWithPlaceByContentIn(@Param("contents") List<Content> contents);
}
