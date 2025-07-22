package org.example.controller;

import org.example.Post;
import org.example.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    // Create a new post
    @PostMapping
    public ResponseEntity<Post> createPost(@RequestBody Post post) {
        Post savedPost = postService.savePost(post);
        return ResponseEntity.ok(savedPost);
    }

    // Get all posts
    @GetMapping
    public ResponseEntity<List<Post>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    // Get a post by ID
    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable Long id) {
        Optional<Post> post = postService.getPostById(id);
        return post.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    // Update a post
    @PutMapping("/{id}")
    public ResponseEntity<Post> updatePost(@PathVariable Long id, @RequestBody Post postDetails) {
        Optional<Post> optionalPost = postService.getPostById(id);
        if (optionalPost.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Post post = optionalPost.get();
        post.setTitle(postDetails.getTitle());
        post.setContent(postDetails.getContent());
        post.setUpdatedAt(java.time.LocalDateTime.now());
        // Update other fields as needed
        return ResponseEntity.ok(postService.savePost(post));
    }

    // Delete a post
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}