package com.broadblog.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.broadblog.entity.Comment;
import com.broadblog.entity.Post;
import com.broadblog.entity.User;
import com.broadblog.repository.CommentRepository;
import com.broadblog.repository.PostRepository;
import com.broadblog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommentService {
    
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    
    @Autowired
    public CommentService(CommentRepository commentRepository, 
                        UserRepository userRepository, 
                        PostRepository postRepository) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }
    
    public Comment saveComment(Comment comment) {
        // Ensure author is a managed entity
        if (comment.getAuthor() != null && comment.getAuthor().getId() != null) {
            User author = userRepository.findById(comment.getAuthor().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
            comment.setAuthor(author);
        } else {
            throw new RuntimeException("Author is required");
        }
        
        // Ensure post is a managed entity
        if (comment.getPost() != null && comment.getPost().getId() != null) {
            Post post = postRepository.findById(comment.getPost().getId())
                .orElseThrow(() -> new RuntimeException("Post not found"));
            comment.setPost(post);
        } else {
            throw new RuntimeException("Post is required");
        }
        
        // Set timestamps
        if (comment.getCreatedAt() == null) {
            comment.setCreatedAt(LocalDateTime.now());
        }
        if (comment.getUpdatedAt() == null) {
            comment.setUpdatedAt(LocalDateTime.now());
        }
        
        return commentRepository.save(comment);
    }
    
    public List<Comment> getAllComments() {
        return commentRepository.findAll();
    }
    
    public Optional<Comment> getCommentById(Long id) {
        return commentRepository.findById(id);
    }
    
    public List<Comment> getCommentsByAuthorId(Long authorId) {
        return commentRepository.findByAuthorId(authorId);
    }
    
    public List<Comment> getCommentsByPostId(Long postId) {
        return commentRepository.findByPostId(postId);
    }
    
    public Comment updateComment(Long id, Comment commentDetails) {
        Optional<Comment> optionalComment = commentRepository.findById(id);
        if (optionalComment.isEmpty()) {
            throw new RuntimeException("Comment not found");
        }
        
        Comment comment = optionalComment.get();
        comment.setContent(commentDetails.getContent());
        comment.setUpdatedAt(LocalDateTime.now());
        
        return commentRepository.save(comment);
    }
    
    public void deleteComment(Long id) {
        if (!commentRepository.existsById(id)) {
            throw new RuntimeException("Comment not found");
        }
        commentRepository.deleteById(id);
    }
} 