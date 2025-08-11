package com.broadblog.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.broadblog.entity.Tag;
import com.broadblog.repository.TagRepository;

@Service
public class TagService {
    
    private final TagRepository tagRepository;
    private final CacheService cacheService;
    
    @Autowired
    public TagService(TagRepository tagRepository, CacheService cacheService) {
        this.tagRepository = tagRepository;
        this.cacheService = cacheService;
    }
    
    @CacheEvict(value = {"tags", "popularTags", "tagCloud", "tagStats"}, allEntries = true)
    public Tag saveTag(Tag tag) {
        Tag savedTag = tagRepository.save(tag);
        clearTagRelatedCache();
        return savedTag;
    }
    
    @Cacheable(value = "tags", key = "'all'")
    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }
    
    @Cacheable(value = "tags", key = "#id")
    public Optional<Tag> getTagById(Long id) {
        return tagRepository.findById(id);
    }
    
    @Cacheable(value = "tags", key = "'name:' + #name")
    public Optional<Tag> getTagByName(String name) {
        return tagRepository.findByName(name);
    }
    
    @CacheEvict(value = {"tags", "popularTags", "tagCloud", "tagStats"}, allEntries = true)
    public Tag updateTag(Long id, Tag tagDetails) {
        Optional<Tag> optionalTag = tagRepository.findById(id);
        if (optionalTag.isEmpty()) {
            throw new RuntimeException("Tag not found");
        }
        
        Tag tag = optionalTag.get();
        tag.setName(tagDetails.getName());
        
        Tag updatedTag = tagRepository.save(tag);
        clearTagRelatedCache();
        return updatedTag;
    }
    
    @CacheEvict(value = {"tags", "popularTags", "tagCloud", "tagStats"}, allEntries = true)
    public void deleteTag(Long id) {
        if (!tagRepository.existsById(id)) {
            throw new RuntimeException("Tag not found");
        }
        tagRepository.deleteById(id);
        clearTagRelatedCache();
    }
    
    // 获取热门标签（使用次数 > 0，按使用次数降序）- 带缓存
    @Cacheable(value = "popularTags", key = "'all'")
    public List<Tag> getPopularTags() {
        return tagRepository.findPopularTags();
    }
    
    // 获取标签云（前20个最热门的标签）- 带缓存
    @Cacheable(value = "tagCloud", key = "'top20'")
    public List<Tag> getTagCloud() {
        return tagRepository.findTop20ByOrderByUsageCountDesc();
    }
    
    // 获取标签统计信息 - 带缓存
    @Cacheable(value = "tagStats", key = "'byCategory'")
    public List<Object[]> getTagStatistics() {
        return tagRepository.countTagsByCategory();
    }
    
    // 增加标签使用次数（当帖子添加标签时调用）
    @CacheEvict(value = {"popularTags", "tagCloud", "tagStats"}, allEntries = true)
    public void incrementTagUsage(Long tagId) {
        Optional<Tag> optionalTag = tagRepository.findById(tagId);
        if (optionalTag.isPresent()) {
            Tag tag = optionalTag.get();
            tag.incrementUsageCount();
            tagRepository.save(tag);
            
            
            updateTagUsageInCache(tag);
        }
    }
    
    // 减少标签使用次数（当帖子移除标签时调用）
    @CacheEvict(value = {"popularTags", "tagCloud", "tagStats"}, allEntries = true)
    public void decrementTagUsage(Long tagId) {
        Optional<Tag> optionalTag = tagRepository.findById(tagId);
        if (optionalTag.isPresent()) {
            Tag tag = optionalTag.get();
            tag.decrementUsageCount();
            tagRepository.save(tag);
            
            // 更新Redis中的标签使用计数缓存
            updateTagUsageInCache(tag);
        }
    }
    
    // 根据分类ID获取标签
    @Cacheable(value = "tags", key = "'category:' + #categoryId")
    public List<Tag> getTagsByCategory(Long categoryId) {
        return tagRepository.findByCategoryId(categoryId);
    }
    
    // 根据分类获取标签
    @Cacheable(value = "tags", key = "'category:' + #category.id")
    public List<Tag> getTagsByCategory(com.broadblog.entity.TagCategory category) {
        return tagRepository.findByCategory(category);
    }
    

    public List<Tag> getTopPopularTags(int top) {
        String popularTagsKey = "popular_tags_ranking";
        
        // 从Redis获取热门标签ID列表
        java.util.Set<Object> topTagIds = cacheService.zRevRange(popularTagsKey, 0, top - 1);
        
        if (topTagIds == null || topTagIds.isEmpty()) {
            // 缓存中没有数据，从数据库获取并构建缓存
            return buildPopularTagsCache(top);
        }
        
        // 根据ID列表获取标签详情
        List<Tag> topTags = topTagIds.stream()
            .map(id -> tagRepository.findById(Long.valueOf(id.toString())))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(java.util.stream.Collectors.toList());
        
        return topTags;
    }
    
    /**
     * 构建热门标签缓存
     * 从数据库获取标签，按使用次数排序，存入Redis
     */
    private List<Tag> buildPopularTagsCache(int top) {
        List<Tag> popularTags = tagRepository.findPopularTags();
        
        // 取前N个热门标签
        List<Tag> topTags = popularTags.stream()
            .limit(top)
            .collect(java.util.stream.Collectors.toList());
        
        // 将结果存入Redis缓存
        String popularTagsKey = "popular_tags_ranking";
        for (Tag tag : topTags) {
            // 使用标签使用次数作为分数
            double score = tag.getUsageCount() != null ? tag.getUsageCount().doubleValue() : 0.0;
            cacheService.zAdd(popularTagsKey, tag.getId().toString(), score);
        }
        
        // 设置缓存过期时间
        cacheService.expire(popularTagsKey, java.time.Duration.ofHours(6));
        
        return topTags;
    }
    
    /**
     * 更新Redis中的标签使用计数缓存
     */
    private void updateTagUsageInCache(Tag tag) {
        String popularTagsKey = "popular_tags_ranking";
        String tagUsageKey = "tag:" + tag.getId() + ":usage";
        
        // 更新标签使用计数缓存
        cacheService.set(tagUsageKey, tag.getUsageCount() != null ? tag.getUsageCount().longValue() : 0L, java.time.Duration.ofDays(7));
        
        // 更新热门标签排行
        double score = tag.getUsageCount() != null ? tag.getUsageCount().doubleValue() : 0.0;
        cacheService.zAdd(popularTagsKey, tag.getId().toString(), score);
        
        // 设置热门标签排行的过期时间
        cacheService.expire(popularTagsKey, java.time.Duration.ofHours(6));
        
        System.out.println("tag usage count updated: " + tag.getName() + " -> " + tag.getUsageCount());
    }
    
    /**
     * 获取标签使用次数（从缓存）
     */
    public Long getTagUsageCount(Long tagId) {
        String tagUsageKey = "tag:" + tagId + ":usage";
        Optional<Long> usage = cacheService.get(tagUsageKey, Long.class);
        
        if (usage.isPresent()) {
            return usage.get();
        }
        
        // 缓存中没有，从数据库获取
        Optional<Tag> tag = tagRepository.findById(tagId);
        if (tag.isPresent()) {
            Integer count = tag.get().getUsageCount();
            // 存入缓存
            cacheService.set(tagUsageKey, count != null ? count.longValue() : 0L, java.time.Duration.ofDays(7));
            return count != null ? count.longValue() : 0L;
        }
        
        return 0L;
    }
    
    /**
     * 清除标签相关的缓存
     */
    private void clearTagRelatedCache() {
        // 清除标签基础信息缓存
        cacheService.clearByPrefix("tags::");
        
        // 清除热门标签缓存
        cacheService.delete("popularTags::all");
        cacheService.delete("popular_tags_ranking");
        
        // 清除标签云缓存
        cacheService.delete("tagCloud::top20");
        
        // 清除标签统计缓存
        cacheService.delete("tagStats::byCategory");
        
        // 清除标签使用计数缓存
        cacheService.clearByPrefix("tag::*:usage");
        
        System.out.println("clear tag related cache");
    }
    
    /**
     * 预热标签缓存
     * 在应用启动时调用，预先加载热门数据到缓存
     */
    public void warmUpTagCache() {
        System.out.println("warm up tag cache");
        
        // 预热热门标签
        getPopularTags();
        
        // 预热标签云
        getTagCloud();
        
        // 预热标签统计
        getTagStatistics();
        
        // 构建热门标签排行缓存
        buildPopularTagsCache(20);
        
        System.out.println("warm up tag cache done");
    }
} 