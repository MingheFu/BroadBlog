package com.broadblog.mapper;

import com.broadblog.dto.CommentDTO;
import com.broadblog.entity.Comment;
import com.broadblog.entity.User;
import com.broadblog.entity.Post;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {
    
    public CommentDTO toDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());
        
        if (comment.getAuthor() != null) {
            dto.setAuthorId(comment.getAuthor().getId());
            dto.setAuthorName(comment.getAuthor().getUsername());
        }
        
        if (comment.getPost() != null) {
            dto.setPostId(comment.getPost().getId());
            dto.setPostTitle(comment.getPost().getTitle());
        }
        
        return dto;
    }
    
    public Comment toEntity(CommentDTO dto) {
        Comment comment = new Comment();
        comment.setContent(dto.getContent());
        comment.setCreatedAt(dto.getCreatedAt());
        comment.setUpdatedAt(dto.getUpdatedAt());
        
        // Set author relationship
        if (dto.getAuthorId() != null) {
            User author = new User();
            author.setId(dto.getAuthorId());
            comment.setAuthor(author);
        }
        
        // Set post relationship
        if (dto.getPostId() != null) {
            Post post = new Post();
            post.setId(dto.getPostId());
            comment.setPost(post);
        }
        
        return comment;
    }
} 