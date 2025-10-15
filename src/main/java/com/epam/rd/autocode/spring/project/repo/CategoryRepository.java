package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category,Long> {
    Optional<Category> findByName(String name);
    boolean existsByName(String name);
}
