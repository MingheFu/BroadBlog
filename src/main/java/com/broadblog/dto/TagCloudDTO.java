package com.broadblog.dto;

public class TagCloudDTO {
    private Long id;
    private String name;
    private String description;
    private Integer usageCount;
    private String categoryName;
    
    // 构造函数
    public TagCloudDTO() {}
    
    public TagCloudDTO(Long id, String name, String description, Integer usageCount, String categoryName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.usageCount = usageCount;
        this.categoryName = categoryName;
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Integer getUsageCount() {
        return usageCount;
    }
    
    public void setUsageCount(Integer usageCount) {
        this.usageCount = usageCount;
    }
    
    public String getCategoryName() {
        return categoryName;
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}