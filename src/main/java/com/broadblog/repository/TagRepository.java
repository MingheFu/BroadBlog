package com.broadblog.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.broadblog.entity.Tag;
import com.broadblog.entity.TagCategory;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);
    
    // 按分类查找标签
    List<Tag> findByCategory(TagCategory category);
    List<Tag> findByCategoryId(Long categoryId);
    
    // 按使用次数排序（标签云）
    List<Tag> findTop20ByOrderByUsageCountDesc();
    
    // 查找热门标签
    @Query("SELECT t FROM Tag t WHERE t.usageCount > 0 ORDER BY t.usageCount DESC")
    List<Tag> findPopularTags();
    
    // 按分类统计标签数量
    @Query("SELECT t.category.name, COUNT(t) FROM Tag t GROUP BY t.category.name")
    List<Object[]> countTagsByCategory();
} 