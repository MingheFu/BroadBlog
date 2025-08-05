package com.broadblog.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.broadblog.dto.PostDTO;
import com.broadblog.entity.Post;
import com.broadblog.entity.Tag;
import com.broadblog.entity.User;
import com.broadblog.service.TagService;

@Component
public class PostMapper {
    
    private final TagService tagService;
    
    @Autowired
    public PostMapper(TagService tagService) {
        this.tagService = tagService;
    }
    
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
        
        // 处理标签：将标签名称转换为标签实体
        if (dto.getTagNames() != null && !dto.getTagNames().isEmpty()) {
            List<Tag> tags = new ArrayList<>();
            for (String tagName : dto.getTagNames()) {
                // 尝试查找现有标签，如果不存在则创建新标签
                Tag tag = tagService.getTagByName(tagName)
                    .orElseGet(() -> {
                        Tag newTag = new Tag();
                        newTag.setName(tagName);
                        return tagService.saveTag(newTag);
                    });
                tags.add(tag);
            }
            post.setTags(tags);
        }
        
        return post;
    }
} 