package com.broadblog.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.broadblog.dto.CommentDTO;
import com.broadblog.entity.Comment;
import com.broadblog.entity.Post;
import com.broadblog.entity.User;
import com.broadblog.repository.CommentRepository;
import com.broadblog.repository.PostRepository;
import com.broadblog.repository.UserRepository;

@Service
public class CommentService {

    private final NotificationService notificationService;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public CommentService(NotificationService notificationService, CommentRepository commentRepository,
                         UserRepository userRepository, PostRepository postRepository) {
        this.notificationService = notificationService;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    /**
     * 添加新评论
     */
    public CommentDTO addComment(CommentDTO commentDto, String postAuthorId) {
        // 转换为实体并保存到数据库
        Comment comment = convertToEntity(commentDto);
        Comment savedComment = commentRepository.save(comment);
        
        // 发送通知给文章作者
        notificationService.sendNewCommentNotification(postAuthorId, savedComment);
        
        return convertToDto(savedComment);
    }

    /**
     * 回复评论
     */
    public CommentDTO replyToComment(CommentDTO replyDto, String originalCommentAuthorId) {
        // 转换为实体并保存到数据库
        Comment reply = convertToEntity(replyDto);
        Comment savedReply = commentRepository.save(reply);
        
        // 发送通知给被回复的评论作者
        notificationService.sendCommentReplyNotification(originalCommentAuthorId, savedReply);
        
        return convertToDto(savedReply);
    }

    /**
     * 点赞评论
     */
    public CommentDTO likeComment(CommentDTO likeDto, String commentAuthorId) {
        // 转换为实体并保存到数据库
        Comment like = convertToEntity(likeDto);
        Comment savedLike = commentRepository.save(like);
        
        // 发送通知给被点赞的评论作者
        notificationService.sendLikeNotification(
            commentAuthorId, 
            like.getAuthor().getId().toString(), 
            like.getAuthor().getUsername(), 
            like.getAuthor().getAvatar(),
            "评论",
            like.getPost().getId().toString(),
            like.getPost().getTitle()
        );
        
        return convertToDto(savedLike);
    }

    /**
     * 删除评论
     */
    public void deleteComment(Long commentId) {
        // 从数据库删除评论（软删除）
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new RuntimeException("Comment not found"));
        comment.setUpdatedAt(java.time.LocalDateTime.now());
        commentRepository.save(comment);
        
        // 这里可以通过 WebSocket 发送删除通知
    }

    /**
     * 根据帖子ID获取评论列表
     */
    public List<Comment> getCommentsByPostId(Long postId) {
        return commentRepository.findByPostId(postId);
    }

    /**
     * 根据作者ID获取评论列表
     */
    public List<Comment> getCommentsByAuthorId(Long authorId) {
        return commentRepository.findByAuthorId(authorId);
    }

    /**
     * 获取评论回复列表
     */
    public List<Comment> getCommentReplies(Long parentCommentId) {
        return commentRepository.findByParentCommentId(parentCommentId);
    }

    /**
     * 统计帖子评论数量
     */
    public long getCommentCountByPostId(Long postId) {
        return commentRepository.countByPostId(postId);
    }

    /**
     * 获取所有评论
     */
    public List<Comment> getAllComments() {
        return commentRepository.findAll();
    }

    /**
     * 根据ID获取评论
     */
    public Optional<Comment> getCommentById(Long id) {
        return commentRepository.findById(id);
    }

    /**
     * 保存评论
     */
    public Comment saveComment(Comment comment) {
        return commentRepository.save(comment);
    }

    /**
     * 将 DTO 转换为实体
     */
    private Comment convertToEntity(CommentDTO dto) {
        Comment entity = new Comment();
        entity.setContent(dto.getContent());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setUpdatedAt(dto.getUpdatedAt());
        
        // 设置作者关系
        if (dto.getAuthorId() != null) {
            User author = userRepository.findById(Long.valueOf(dto.getAuthorId()))
                .orElseThrow(() -> new RuntimeException("User not found"));
            entity.setAuthor(author);
        }
        
        // 设置帖子关系
        if (dto.getPostId() != null) {
            Post post = postRepository.findById(Long.valueOf(dto.getPostId()))
                .orElseThrow(() -> new RuntimeException("Post not found"));
            entity.setPost(post);
        }
        
        return entity;
    }

    /**
     * 将实体转换为 DTO
     */
    private CommentDTO convertToDto(Comment entity) {
        return new CommentDTO(
            entity.getId(),
            entity.getContent(),
            entity.getAuthor() != null ? entity.getAuthor().getId().toString() : null,
            entity.getAuthor() != null ? entity.getAuthor().getUsername() : null,
            entity.getAuthor() != null ? entity.getAuthor().getAvatar() : null,
            entity.getPost() != null ? entity.getPost().getId().toString() : null,
            entity.getPost() != null ? entity.getPost().getTitle() : null,
            entity.getCreatedAt(),
            "NEW_COMMENT", // 默认类型
            null, // parentCommentId
            false, // isDeleted
            entity.getUpdatedAt()
        );
    }
} 