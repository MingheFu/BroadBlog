package com.broadblog.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.broadblog.dto.CommentDTO;
import com.broadblog.entity.Comment;
import com.broadblog.mapper.CommentMapper;
import com.broadblog.security.CustomUserDetails;
import com.broadblog.service.CommentService;
import com.broadblog.service.UserService;

@RestController
@RequestMapping("/api/comments")
public class CommentController {
    
    private final CommentService commentService;
    private final CommentMapper commentMapper;
    private final UserService userService;
    
    @Autowired
    public CommentController(CommentService commentService, CommentMapper commentMapper, UserService userService) {
        this.commentService = commentService;
        this.commentMapper = commentMapper;
        this.userService = userService;
    }

    // 获取当前用户ID
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return userDetails.getUser().getId();
        }
        throw new RuntimeException("User not authenticated");
    }

    // 检查权限：用户只能管理自己的评论，管理员可以管理所有评论
    private boolean hasPermission(Long commentAuthorId) {
        Long currentUserId = getCurrentUserId();
        // 如果是管理自己的评论，允许
        if (currentUserId.equals(commentAuthorId)) {
            return true;
        }
        // 如果是管理员，允许管理所有评论
        return userService.isAdmin(currentUserId);
    }
    
    @PostMapping
    public ResponseEntity<?> createComment(@RequestBody CommentDTO commentDTO) {
        try {
            // 获取当前登录用户ID
            Long currentUserId = getCurrentUserId();
            commentDTO.setAuthorId(currentUserId.toString()); // 强制设置作者为当前登录用户
            
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
    public ResponseEntity<List<CommentDTO>> getCommentsByAuthorId(@PathVariable String authorId) {
        List<Comment> comments = commentService.getCommentsByAuthorId(Long.valueOf(authorId));
        List<CommentDTO> commentDTOs = comments.stream()
            .map(commentMapper::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(commentDTOs);
    }
    
    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentDTO>> getCommentsByPostId(@PathVariable String postId) {
        List<Comment> comments = commentService.getCommentsByPostId(Long.valueOf(postId));
        List<CommentDTO> commentDTOs = comments.stream()
            .map(commentMapper::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(commentDTOs);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateComment(@PathVariable Long id, @RequestBody CommentDTO commentDTO) {
        try {
            Optional<Comment> optionalComment = commentService.getCommentById(id);
            if (optionalComment.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Comment comment = optionalComment.get();
            
            // 权限验证：只能编辑自己的评论，管理员可以编辑所有评论
            if (!hasPermission(comment.getAuthor().getId())) {
                return ResponseEntity.status(403).build(); // 403 Forbidden
            }
            
            comment.setContent(commentDTO.getContent());
            comment.setUpdatedAt(LocalDateTime.now());
            
            Comment updatedComment = commentService.saveComment(comment);
            return ResponseEntity.ok(commentMapper.toDTO(updatedComment));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteComment(@PathVariable Long id) {
        try {
            Optional<Comment> optionalComment = commentService.getCommentById(id);
            if (optionalComment.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Comment comment = optionalComment.get();
            
            // 权限验证：只能删除自己的评论，管理员可以删除所有评论
            if (!hasPermission(comment.getAuthor().getId())) {
                return ResponseEntity.status(403).build(); // 403 Forbidden
            }
            
            commentService.deleteComment(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
} 