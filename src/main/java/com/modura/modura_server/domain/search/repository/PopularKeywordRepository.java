package com.modura.modura_server.domain.search.repository;

import com.modura.modura_server.domain.search.entity.PopularKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PopularKeywordRepository extends JpaRepository<PopularKeyword, Long> {

}
