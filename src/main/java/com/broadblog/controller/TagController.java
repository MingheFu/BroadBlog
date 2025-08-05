package com.broadblog.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.broadblog.dto.TagCloudDTO;
import com.broadblog.dto.TagDTO;
import com.broadblog.entity.Tag;
import com.broadblog.mapper.TagMapper;
import com.broadblog.service.TagService;

@RestController
@RequestMapping("/api/tags")
public class TagController {
    
    private final TagService tagService;
    private final TagMapper tagMapper;
    
    @Autowired
    public TagController(TagService tagService, TagMapper tagMapper) {
        this.tagService = tagService;
        this.tagMapper = tagMapper;
    }
    
    @PostMapping
    public ResponseEntity<TagDTO> createTag(@RequestBody TagDTO tagDTO) {
        try {
            Tag tag = tagMapper.toEntity(tagDTO);
            Tag savedTag = tagService.saveTag(tag);
            return ResponseEntity.ok(tagMapper.toDTO(savedTag));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(null);
        }
    }
    
    @GetMapping
    public ResponseEntity<List<TagDTO>> getAllTags() {
        List<Tag> tags = tagService.getAllTags();
        List<TagDTO> tagDTOs = tags.stream()
            .map(tagMapper::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(tagDTOs);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TagDTO> getTagById(@PathVariable Long id) {
        Optional<Tag> tag = tagService.getTagById(id);
        return tag.map(t -> ResponseEntity.ok(tagMapper.toDTO(t)))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/name/{name}")
    public ResponseEntity<TagDTO> getTagByName(@PathVariable String name) {
        Optional<Tag> tag = tagService.getTagByName(name);
        return tag.map(t -> ResponseEntity.ok(tagMapper.toDTO(t)))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TagDTO> updateTag(@PathVariable Long id, @RequestBody TagDTO tagDTO) {
        try {
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
                .body(null);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        try {
            tagService.deleteTag(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // 获取热门标签（使用次数 > 0）
    @GetMapping("/popular")
    public ResponseEntity<List<TagCloudDTO>> getPopularTags() {
        List<Tag> popularTags = tagService.getPopularTags();
        List<TagCloudDTO> tagCloudDTOs = popularTags.stream()
            .map(this::convertToTagCloudDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(tagCloudDTOs);
    }
    
    // 获取标签云（前20个最热门标签）
    @GetMapping("/cloud")
    public ResponseEntity<List<TagCloudDTO>> getTagCloud() {
        List<Tag> tagCloud = tagService.getTagCloud();
        List<TagCloudDTO> tagCloudDTOs = tagCloud.stream()
            .map(this::convertToTagCloudDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(tagCloudDTOs);
    }
    
    // 获取标签统计信息
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getTagStatistics() {
        List<Object[]> statistics = tagService.getTagStatistics();
        
        Map<String, Object> result = new HashMap<>();
        Map<String, Long> categoryStats = new HashMap<>();
        
        for (Object[] stat : statistics) {
            String categoryName = (String) stat[0];
            Long count = (Long) stat[1];
            categoryStats.put(categoryName != null ? categoryName : "未分类", count);
        }
        
        result.put("totalTags", tagService.getAllTags().size());
        result.put("categoryStats", categoryStats);
        result.put("popularTagsCount", tagService.getPopularTags().size());
        
        return ResponseEntity.ok(result);
    }
    
    // 辅助方法：将 Tag 转换为 TagCloudDTO
    private TagCloudDTO convertToTagCloudDTO(Tag tag) {
        return new TagCloudDTO(
            tag.getId(),
            tag.getName(),
            tag.getDescription(),
            tag.getUsageCount(),
            tag.getCategory() != null ? tag.getCategory().getName() : null
        );
    }
} 