package com.broadblog.dto;

import java.util.List;

public class TagDTO {
    private Long id;
    private String name;
    private String description;
    private Long categoryId;
    private String categoryName;
    private Integer usageCount;
    private List<String> postTitles;

    // Default constructor
    public TagDTO() {}

    // Constructor with all fields
    public TagDTO(Long id, String name, String description, Long categoryId, String categoryName, Integer usageCount, List<String> postTitles) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.usageCount = usageCount;
        this.postTitles = postTitles;
    }

    // Constructor with id and name
    public TagDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getPostTitles() {
        return postTitles;
    }

    public void setPostTitles(List<String> postTitles) {
        this.postTitles = postTitles;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Integer getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(Integer usageCount) {
        this.usageCount = usageCount;
    }
} 