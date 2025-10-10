package com.modura.modura_server.domain.user.repository;

import com.modura.modura_server.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}
