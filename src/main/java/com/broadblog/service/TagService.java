package com.broadblog.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.broadblog.entity.Tag;
import com.broadblog.repository.TagRepository;

@Service
public class TagService {
    
    private final TagRepository tagRepository;
    
    @Autowired
    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }
    
    public Tag saveTag(Tag tag) {
        return tagRepository.save(tag);
    }
    
    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }
    
    public Optional<Tag> getTagById(Long id) {
        return tagRepository.findById(id);
    }
    
    public Optional<Tag> getTagByName(String name) {
        return tagRepository.findByName(name);
    }
    
    public Tag updateTag(Long id, Tag tagDetails) {
        Optional<Tag> optionalTag = tagRepository.findById(id);
        if (optionalTag.isEmpty()) {
            throw new RuntimeException("Tag not found");
        }
        
        Tag tag = optionalTag.get();
        tag.setName(tagDetails.getName());
        
        return tagRepository.save(tag);
    }
    
    public void deleteTag(Long id) {
        if (!tagRepository.existsById(id)) {
            throw new RuntimeException("Tag not found");
        }
        tagRepository.deleteById(id);
    }
    
    // 获取热门标签（使用次数 > 0，按使用次数降序）
    public List<Tag> getPopularTags() {
        return tagRepository.findPopularTags();
    }
    
    // 获取标签云（前20个最热门的标签）
    public List<Tag> getTagCloud() {
        return tagRepository.findTop20ByOrderByUsageCountDesc();
    }
    
    // 获取标签统计信息
    public List<Object[]> getTagStatistics() {
        return tagRepository.countTagsByCategory();
    }
    
    // 增加标签使用次数（当帖子添加标签时调用）
    public void incrementTagUsage(Long tagId) {
        Optional<Tag> optionalTag = tagRepository.findById(tagId);
        if (optionalTag.isPresent()) {
            Tag tag = optionalTag.get();
            tag.incrementUsageCount();
            tagRepository.save(tag);
        }
    }
    
    // 减少标签使用次数（当帖子移除标签时调用）
    public void decrementTagUsage(Long tagId) {
        Optional<Tag> optionalTag = tagRepository.findById(tagId);
        if (optionalTag.isPresent()) {
            Tag tag = optionalTag.get();
            tag.decrementUsageCount();
            tagRepository.save(tag);
        }
    }
    
    // 根据分类ID获取标签
    public List<Tag> getTagsByCategory(Long categoryId) {
        return tagRepository.findByCategoryId(categoryId);
    }
    
    // 根据分类获取标签
    public List<Tag> getTagsByCategory(com.broadblog.entity.TagCategory category) {
        return tagRepository.findByCategory(category);
    }
} 