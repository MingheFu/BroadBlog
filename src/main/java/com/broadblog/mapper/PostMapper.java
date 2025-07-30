package com.broadblog.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.broadblog.dto.PostDTO;
import com.broadblog.entity.Post;
import com.broadblog.entity.Tag;
import com.broadblog.entity.User;
import org.springframework.stereotype.Component;

@Component
public class PostMapper {
    
    public PostDTO toDTO(Post post) {
        PostDTO dto = new PostDTO();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUpdatedAt(post.getUpdatedAt());
        
        // 设置作者信息
        if (post.getAuthor() != null) {
            dto.setAuthorId(post.getAuthor().getId());
            dto.setAuthorName(post.getAuthor().getUsername());
        }
        
        // 设置标签信息
        if (post.getTags() != null) {
            List<String> tagNames = post.getTags().stream()
                .map(Tag::getName)
                .collect(Collectors.toList());
            dto.setTagNames(tagNames);
        }
        
        // 设置评论数量
        if (post.getComments() != null) {
            dto.setCommentCount(post.getComments().size());
        }
        
        return dto;
    }
    
    public Post toEntity(PostDTO dto) {
        Post post = new Post();
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setCreatedAt(dto.getCreatedAt());
        post.setUpdatedAt(dto.getUpdatedAt());
        
        // 设置 author，如果 authorId 不为空
        if (dto.getAuthorId() != null) {
            User author = new User();
            author.setId(dto.getAuthorId());
            post.setAuthor(author);
        }
        
        return post;
    }
} 