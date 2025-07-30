package com.broadblog.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.broadblog.dto.CommentDTO;
import com.broadblog.entity.Comment;
import com.broadblog.mapper.CommentMapper;
import com.broadblog.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments")
public class CommentController {
    
    private final CommentService commentService;
    private final CommentMapper commentMapper;
    
    @Autowired
    public CommentController(CommentService commentService, CommentMapper commentMapper) {
        this.commentService = commentService;
        this.commentMapper = commentMapper;
    }
    
    @PostMapping
    public ResponseEntity<?> createComment(@RequestBody CommentDTO commentDTO) {
        try {
            Comment comment = commentMapper.toEntity(commentDTO);
            comment.setCreatedAt(LocalDateTime.now());
            comment.setUpdatedAt(LocalDateTime.now());
            Comment savedComment = commentService.saveComment(comment);
            return ResponseEntity.ok(commentMapper.toDTO(savedComment));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
    
    @GetMapping
    public ResponseEntity<List<CommentDTO>> getAllComments() {
        List<Comment> comments = commentService.getAllComments();
        List<CommentDTO> commentDTOs = comments.stream()
            .map(commentMapper::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(commentDTOs);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CommentDTO> getCommentById(@PathVariable Long id) {
        Optional<Comment> comment = commentService.getCommentById(id);
        return comment.map(c -> ResponseEntity.ok(commentMapper.toDTO(c)))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/author/{authorId}")
    public ResponseEntity<List<CommentDTO>> getCommentsByAuthorId(@PathVariable Long authorId) {
        List<Comment> comments = commentService.getCommentsByAuthorId(authorId);
        List<CommentDTO> commentDTOs = comments.stream()
            .map(commentMapper::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(commentDTOs);
    }
    
    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentDTO>> getCommentsByPostId(@PathVariable Long postId) {
        List<Comment> comments = commentService.getCommentsByPostId(postId);
        List<CommentDTO> commentDTOs = comments.stream()
            .map(commentMapper::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(commentDTOs);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<CommentDTO> updateComment(@PathVariable Long id, @RequestBody CommentDTO commentDTO) {
        try {
            Optional<Comment> optionalComment = commentService.getCommentById(id);
            if (optionalComment.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Comment comment = optionalComment.get();
            comment.setContent(commentDTO.getContent());
            comment.setUpdatedAt(LocalDateTime.now());
            
            Comment updatedComment = commentService.saveComment(comment);
            return ResponseEntity.ok(commentMapper.toDTO(updatedComment));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(null);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        try {
            commentService.deleteComment(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
} 