package com.modura.modura_server.domain.content.repository;

import com.modura.modura_server.domain.content.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

}
