package com.modura.modura_server.domain.content.repository;

import com.modura.modura_server.domain.content.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentRepository extends JpaRepository<Content, Long> {

}
