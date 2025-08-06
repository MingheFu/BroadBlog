package com.broadblog.controller;

import java.util.List;
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

import com.broadblog.dto.TagCategoryDTO;
import com.broadblog.entity.TagCategory;
import com.broadblog.mapper.TagCategoryMapper;
import com.broadblog.service.TagCategoryService;

@RestController
@RequestMapping("/api/tag-categories")
public class TagCategoryController {
    
    private final TagCategoryService tagCategoryService;
    private final TagCategoryMapper tagCategoryMapper;
    
    @Autowired
    public TagCategoryController(TagCategoryService tagCategoryService, TagCategoryMapper tagCategoryMapper) {
        this.tagCategoryService = tagCategoryService;
        this.tagCategoryMapper = tagCategoryMapper;
    }
    
    // 创建新分类
    @PostMapping
    public ResponseEntity<?> createTagCategory(@RequestBody TagCategoryDTO tagCategoryDTO) {
        try {
            // 检查分类名称是否已存在
            if (tagCategoryService.existsByName(tagCategoryDTO.getName())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("{\"error\": \"Category name already exists\"}");
            }
            
            TagCategory tagCategory = tagCategoryMapper.toEntity(tagCategoryDTO);
            TagCategory savedCategory = tagCategoryService.saveTagCategory(tagCategory);
            return ResponseEntity.ok(tagCategoryMapper.toDTO(savedCategory));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
    
    // 获取所有分类
    @GetMapping
    public ResponseEntity<List<TagCategoryDTO>> getAllTagCategories() {
        List<TagCategory> categories = tagCategoryService.getAllTagCategories();
        List<TagCategoryDTO> categoryDTOs = categories.stream()
            .map(tagCategoryMapper::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(categoryDTOs);
    }
    
    // 根据ID获取分类
    @GetMapping("/{id}")
    public ResponseEntity<TagCategoryDTO> getTagCategoryById(@PathVariable Long id) {
        Optional<TagCategory> category = tagCategoryService.getTagCategoryById(id);
        return category.map(c -> ResponseEntity.ok(tagCategoryMapper.toDTO(c)))
            .orElse(ResponseEntity.notFound().build());
    }
    
    // 根据名称获取分类
    @GetMapping("/name/{name}")
    public ResponseEntity<TagCategoryDTO> getTagCategoryByName(@PathVariable String name) {
        Optional<TagCategory> category = tagCategoryService.getTagCategoryByName(name);
        return category.map(c -> ResponseEntity.ok(tagCategoryMapper.toDTO(c)))
            .orElse(ResponseEntity.notFound().build());
    }
    
    // 更新分类
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTagCategory(@PathVariable Long id, @RequestBody TagCategoryDTO tagCategoryDTO) {
        try {
            // 检查名称是否与其他分类冲突
            Optional<TagCategory> existingCategory = tagCategoryService.getTagCategoryByName(tagCategoryDTO.getName());
            if (existingCategory.isPresent() && !existingCategory.get().getId().equals(id)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("{\"error\": \"Category name already exists\"}");
            }
            
            TagCategory updatedCategory = tagCategoryService.updateTagCategory(id, tagCategoryMapper.toEntity(tagCategoryDTO));
            return ResponseEntity.ok(tagCategoryMapper.toDTO(updatedCategory));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
    
    // 删除分类
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTagCategory(@PathVariable Long id) {
        try {
            tagCategoryService.deleteTagCategory(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}