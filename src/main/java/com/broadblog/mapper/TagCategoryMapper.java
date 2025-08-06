package com.broadblog.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.broadblog.dto.TagCategoryDTO;
import com.broadblog.entity.Tag;
import com.broadblog.entity.TagCategory;

@Component
public class TagCategoryMapper {
    
    public TagCategoryDTO toDTO(TagCategory tagCategory) {
        TagCategoryDTO dto = new TagCategoryDTO();
        dto.setId(tagCategory.getId());
        dto.setName(tagCategory.getName());
        dto.setDescription(tagCategory.getDescription());
        
        // 设置该分类下的标签名称列表
        if (tagCategory.getTags() != null) {
            List<String> tagNames = tagCategory.getTags().stream()
                .map(Tag::getName)
                .collect(Collectors.toList());
            dto.setTagNames(tagNames);
        }
        
        return dto;
    }
    
    public TagCategory toEntity(TagCategoryDTO dto) {
        TagCategory tagCategory = new TagCategory();
        tagCategory.setName(dto.getName());
        tagCategory.setDescription(dto.getDescription());
        
        return tagCategory;
    }
}