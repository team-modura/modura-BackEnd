package com.modura.modura_server.domain.user.repository;

import com.modura.modura_server.domain.user.entity.User;
import com.modura.modura_server.domain.user.entity.UserCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCategoryRepository extends JpaRepository<UserCategory, Long> {

    void deleteByUser(User user);
}
