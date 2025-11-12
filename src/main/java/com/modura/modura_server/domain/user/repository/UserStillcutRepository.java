package com.modura.modura_server.domain.user.repository;

import com.modura.modura_server.domain.user.entity.UserStillcut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface UserStillcutRepository extends JpaRepository<UserStillcut, Long> {

    @Query("SELECT us FROM UserStillcut us " +
            "JOIN FETCH us.stillcut s JOIN FETCH s.content c JOIN FETCH s.place p " +
            "WHERE us.user.id = :userId")
    List<UserStillcut> findAllWithDetailsByUserId(@Param("userId") Long userId);
}
