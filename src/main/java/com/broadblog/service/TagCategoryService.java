package com.broadblog.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.broadblog.entity.TagCategory;
import com.broadblog.repository.TagCategoryRepository;

@Service
public class TagCategoryService {
    
    private final TagCategoryRepository tagCategoryRepository;
    
    @Autowired
    public TagCategoryService(TagCategoryRepository tagCategoryRepository) {
        this.tagCategoryRepository = tagCategoryRepository;
    }
    
    // 创建或更新分类
    public TagCategory saveTagCategory(TagCategory tagCategory) {
        return tagCategoryRepository.save(tagCategory);
    }
    
    // 获取所有分类
    public List<TagCategory> getAllTagCategories() {
        return tagCategoryRepository.findAll();
    }
    
    // 根据ID获取分类
    public Optional<TagCategory> getTagCategoryById(Long id) {
        return tagCategoryRepository.findById(id);
    }
    
    // 根据名称获取分类
    public Optional<TagCategory> getTagCategoryByName(String name) {
        return tagCategoryRepository.findByName(name);
    }
    
    // 更新分类
    public TagCategory updateTagCategory(Long id, TagCategory categoryDetails) {
        Optional<TagCategory> optionalCategory = tagCategoryRepository.findById(id);
        if (optionalCategory.isEmpty()) {
            throw new RuntimeException("Tag category not found");
        }
        
        TagCategory category = optionalCategory.get();
        category.setName(categoryDetails.getName());
        category.setDescription(categoryDetails.getDescription());
        
        return tagCategoryRepository.save(category);
    }
    
    // 删除分类
    public void deleteTagCategory(Long id) {
        if (!tagCategoryRepository.existsById(id)) {
            throw new RuntimeException("Tag category not found");
        }
        tagCategoryRepository.deleteById(id);
    }
    
    // 检查分类名称是否已存在
    public boolean existsByName(String name) {
        return tagCategoryRepository.findByName(name).isPresent();
    }
}