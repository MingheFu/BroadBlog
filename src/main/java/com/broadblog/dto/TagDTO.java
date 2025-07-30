package com.broadblog.dto;

import java.util.List;

public class TagDTO {
    private Long id;
    private String name;
    private List<String> postTitles;

    // Default constructor
    public TagDTO() {}

    // Constructor with all fields
    public TagDTO(Long id, String name, List<String> postTitles) {
        this.id = id;
        this.name = name;
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
} 