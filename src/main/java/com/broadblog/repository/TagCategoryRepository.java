package com.broadblog.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.broadblog.entity.TagCategory;

public interface TagCategoryRepository extends JpaRepository<TagCategory, Long> {
    Optional<TagCategory> findByName(String name);
}