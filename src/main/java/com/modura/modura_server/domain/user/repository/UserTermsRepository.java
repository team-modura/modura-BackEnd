package com.modura.modura_server.domain.user.repository;

import com.modura.modura_server.domain.user.entity.UserTerms;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTermsRepository extends JpaRepository<UserTerms, Long> {

}
