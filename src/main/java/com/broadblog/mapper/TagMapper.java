package com.broadblog.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.broadblog.dto.TagDTO;
import com.broadblog.entity.Tag;
import org.springframework.stereotype.Component;

@Component
public class TagMapper {
    
    public TagDTO toDTO(Tag tag) {
        TagDTO dto = new TagDTO();
        dto.setId(tag.getId());
        dto.setName(tag.getName());
        
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
        
        // Note: posts relationship should be set in service layer
        // to ensure they are managed entities
        
        return tag;
    }
} 