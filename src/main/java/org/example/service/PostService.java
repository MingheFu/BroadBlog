package org.example.service;

import java.util.List;
import java.util.Optional;

import org.example.Post;
import org.example.User;
import org.example.repository.PostRepository;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Autowired
    public PostService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    // Create or update a post
    public Post savePost(Post post) {
        if (post.getAuthor() != null && post.getAuthor().getId() != null) {
            User author = userRepository.findById(post.getAuthor().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
            post.setAuthor(author);
        } else {
            throw new RuntimeException("Author is required");
        }
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