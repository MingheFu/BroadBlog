package com.broadblog.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.broadblog.dto.TagDTO;
import com.broadblog.entity.Tag;
import com.broadblog.entity.TagCategory;
import com.broadblog.service.TagCategoryService;

@Component
public class TagMapper {
    
    private final TagCategoryService tagCategoryService;
    
    @Autowired
    public TagMapper(TagCategoryService tagCategoryService) {
        this.tagCategoryService = tagCategoryService;
    }
    
    public TagDTO toDTO(Tag tag) {
        TagDTO dto = new TagDTO();
        dto.setId(tag.getId());
        dto.setName(tag.getName());
        dto.setDescription(tag.getDescription());
        dto.setUsageCount(tag.getUsageCount());
        
        // 设置分类信息
        if (tag.getCategory() != null) {
            dto.setCategoryId(tag.getCategory().getId());
            dto.setCategoryName(tag.getCategory().getName());
        }
        
        if (tag.getPosts() != null) {
            List<String> postTitles = tag.getPosts().stream()
                .map(post -> post.getTitle())
                .collect(Collectors.toList());
            dto.setPostTitles(postTitles);
        }
        
        return dto;
    }
    
    public Tag toEntity(TagDTO dto) {
        Tag tag = new Tag();
        tag.setName(dto.getName());
        tag.setDescription(dto.getDescription());
        tag.setUsageCount(dto.getUsageCount() != null ? dto.getUsageCount() : 0);
        
        // 设置分类
        if (dto.getCategoryId() != null) {
            TagCategory category = tagCategoryService.getTagCategoryById(dto.getCategoryId())
                .orElse(null);
            tag.setCategory(category);
        }
        
        // Note: posts relationship should be set in service layer
        // to ensure they are managed entities
        
        return tag;
    }
} 