package com.modura.modura_server.global.tmdb.repository;

import com.modura.modura_server.global.tmdb.entity.TmdbBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;

public interface TmdbBlacklistRepository extends JpaRepository<TmdbBlacklist, Long> {

    @Query("SELECT t.tmdbId FROM TmdbBlacklist t")
    Set<Integer> findAllTmdbIds();
}
