package org.example.service;

import org.example.Post;
import org.example.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    private final PostRepository postRepository;

    @Autowired
    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    // Create or update a post
    public Post savePost(Post post) {
        return postRepository.save(post);
    }

    // Get all posts
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    // Get a post by ID
    public Optional<Post> getPostById(Long id) {
        return postRepository.findById(id);
    }

    // Delete a post by ID
    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }

    // Custom: Get posts by author
    public List<Post> getPostsByAuthorId(Long authorId) {
        return postRepository.findByAuthorId(authorId);
    }
}