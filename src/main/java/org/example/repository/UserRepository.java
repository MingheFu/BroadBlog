package org.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.example.User;

public interface UserRepository extends JpaRepository<User, Long> {
    // You can add custom query methods here if needed
} 