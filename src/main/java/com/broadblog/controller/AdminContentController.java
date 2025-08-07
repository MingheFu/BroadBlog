package com.broadblog.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.broadblog.dto.CommentDTO;
import com.broadblog.dto.PostDTO;
import com.broadblog.dto.TagDTO;
import com.broadblog.entity.Comment;
import com.broadblog.entity.Post;
import com.broadblog.entity.Tag;
import com.broadblog.mapper.CommentMapper;
import com.broadblog.mapper.PostMapper;
import com.broadblog.mapper.TagMapper;
import com.broadblog.security.CustomUserDetails;
import com.broadblog.service.CommentService;
import com.broadblog.service.PostService;
import com.broadblog.service.TagService;
import com.broadblog.service.UserService;

@RestController
@RequestMapping("/api/admin/content")
public class AdminContentController {

    private final PostService postService;
    private final CommentService commentService;
    private final TagService tagService;
    private final UserService userService;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final TagMapper tagMapper;

    @Autowired
    public AdminContentController(PostService postService, CommentService commentService, 
                                 TagService tagService, UserService userService,
                                 PostMapper postMapper, CommentMapper commentMapper, TagMapper tagMapper) {
        this.postService = postService;
        this.commentService = commentService;
        this.tagService = tagService;
        this.userService = userService;
        this.postMapper = postMapper;
        this.commentMapper = commentMapper;
        this.tagMapper = tagMapper;
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

    // 检查是否为管理员
    private boolean isAdmin() {
        Long currentUserId = getCurrentUserId();
        return userService.isAdmin(currentUserId);
    }

    // ==================== 帖子管理 ====================

    // 管理员获取所有帖子（分页）
    @GetMapping("/posts")
    public ResponseEntity<?> getAllPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            if (!isAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("{\"error\": \"Access denied. Admin role required.\"}");
            }

            Page<Post> pagePosts = postService.getPostsByPage(page, size);
            List<PostDTO> postDTOs = pagePosts.getContent().stream()
                .map(postMapper::toDTO)
                .collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();
            result.put("data", postDTOs);
            result.put("currentPage", page);
            result.put("pageSize", size);
            result.put("totalElements", pagePosts.getTotalElements());
            result.put("totalPages", pagePosts.getTotalPages());

            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("{\"error\": \"User not authenticated\"}");
        }
    }

    // 管理员更新任何帖子
    @PutMapping("/posts/{id}")
    public ResponseEntity<?> updatePost(@PathVariable Long id, @RequestBody PostDTO postDTO) {
        try {
            if (!isAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("{\"error\": \"Access denied. Admin role required.\"}");
            }

            Optional<Post> optionalPost = postService.getPostById(id);
            if (optionalPost.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Post post = optionalPost.get();
            post.setTitle(postDTO.getTitle());
            post.setContent(postDTO.getContent());
            post.setUpdatedAt(java.time.LocalDateTime.now());

            Post updatedPost = postService.savePost(post);
            return ResponseEntity.ok(postMapper.toDTO(updatedPost));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    // 管理员删除任何帖子
    @DeleteMapping("/posts/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id) {
        try {
            if (!isAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("{\"error\": \"Access denied. Admin role required.\"}");
            }

            postService.deletePost(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    // ==================== 评论管理 ====================

    // 管理员获取所有评论（分页）
    @GetMapping("/comments")
    public ResponseEntity<?> getAllComments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            if (!isAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("{\"error\": \"Access denied. Admin role required.\"}");
            }

            List<Comment> comments = commentService.getAllComments();
            List<CommentDTO> commentDTOs = comments.stream()
                .map(commentMapper::toDTO)
                .collect(Collectors.toList());

            // 简单的分页实现
            int start = (page - 1) * size;
            int end = Math.min(start + size, commentDTOs.size());
            List<CommentDTO> pageComments = commentDTOs.subList(start, end);

            Map<String, Object> result = new HashMap<>();
            result.put("data", pageComments);
            result.put("currentPage", page);
            result.put("pageSize", size);
            result.put("totalElements", commentDTOs.size());
            result.put("totalPages", (int) Math.ceil((double) commentDTOs.size() / size));

            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("{\"error\": \"User not authenticated\"}");
        }
    }

    // 管理员更新任何评论
    @PutMapping("/comments/{id}")
    public ResponseEntity<?> updateComment(@PathVariable Long id, @RequestBody CommentDTO commentDTO) {
        try {
            if (!isAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("{\"error\": \"Access denied. Admin role required.\"}");
            }

            Optional<Comment> optionalComment = commentService.getCommentById(id);
            if (optionalComment.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Comment comment = optionalComment.get();
            comment.setContent(commentDTO.getContent());
            comment.setUpdatedAt(java.time.LocalDateTime.now());

            Comment updatedComment = commentService.saveComment(comment);
            return ResponseEntity.ok(commentMapper.toDTO(updatedComment));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    // 管理员删除任何评论
    @DeleteMapping("/comments/{id}")
    public ResponseEntity<?> deleteComment(@PathVariable Long id) {
        try {
            if (!isAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("{\"error\": \"Access denied. Admin role required.\"}");
            }

            commentService.deleteComment(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    // ==================== 标签管理 ====================

    // 管理员获取所有标签
    @GetMapping("/tags")
    public ResponseEntity<?> getAllTags() {
        try {
            if (!isAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("{\"error\": \"Access denied. Admin role required.\"}");
            }

            List<Tag> tags = tagService.getAllTags();
            List<TagDTO> tagDTOs = tags.stream()
                .map(tagMapper::toDTO)
                .collect(Collectors.toList());

            return ResponseEntity.ok(tagDTOs);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("{\"error\": \"User not authenticated\"}");
        }
    }

    // 管理员更新任何标签
    @PutMapping("/tags/{id}")
    public ResponseEntity<?> updateTag(@PathVariable Long id, @RequestBody TagDTO tagDTO) {
        try {
            if (!isAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("{\"error\": \"Access denied. Admin role required.\"}");
            }

            Optional<Tag> optionalTag = tagService.getTagById(id);
            if (optionalTag.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Tag tag = optionalTag.get();
            tag.setName(tagDTO.getName());

            Tag updatedTag = tagService.saveTag(tag);
            return ResponseEntity.ok(tagMapper.toDTO(updatedTag));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    // 管理员删除任何标签
    @DeleteMapping("/tags/{id}")
    public ResponseEntity<?> deleteTag(@PathVariable Long id) {
        try {
            if (!isAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("{\"error\": \"Access denied. Admin role required.\"}");
            }

            tagService.deleteTag(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }


    // 获取内容统计信息
    @GetMapping("/stats")
    public ResponseEntity<?> getContentStats() {
        try {
            if (!isAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("{\"error\": \"Access denied. Admin role required.\"}");
            }

            List<Post> allPosts = postService.getAllPosts();
            List<Comment> allComments = commentService.getAllComments();
            List<Tag> allTags = tagService.getAllTags();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalPosts", allPosts.size());
            stats.put("totalComments", allComments.size());
            stats.put("totalTags", allTags.size());

            return ResponseEntity.ok(stats);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("{\"error\": \"User not authenticated\", \"code\": 401, \"message\": \"Please login first\"}");
        }
    }
} 