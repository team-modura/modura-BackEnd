package com.modura.modura_server.domain.keyword.repository;

import com.modura.modura_server.domain.keyword.entity.PopularKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PopularKeywordRepository extends JpaRepository<PopularKeyword, Long> {

}
