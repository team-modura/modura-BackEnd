package com.modura.modura_server.domain.user.repository;

import com.modura.modura_server.domain.user.entity.Terms;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermsRepository extends JpaRepository<Terms, Long> {

}
