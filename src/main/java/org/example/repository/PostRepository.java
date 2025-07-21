package org.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.example.Post;
import java.util.List;
import org.example.Tag;

public interface PostRepository extends JpaRepository<Post, Long> {
    // You can add custom query methods here if needed
    List<Post> findByAuthorId(Long authorId);
    List<Post> findByTags(List<Tag> tags);
    List<Post> findByTitleContaining(String title);
} 