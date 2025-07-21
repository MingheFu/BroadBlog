package org.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.example.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> {
    // You can add custom query methods here if needed
} 