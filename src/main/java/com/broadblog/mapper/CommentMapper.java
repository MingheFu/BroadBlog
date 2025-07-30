package com.broadblog.mapper;

import com.broadblog.dto.CommentDTO;
import com.broadblog.entity.Comment;
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
        
        // Note: author and post relationships should be set in service layer
        // to ensure they are managed entities
        
        return comment;
    }
} 