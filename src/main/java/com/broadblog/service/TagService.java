package com.broadblog.service;

import java.util.List;
import java.util.Optional;

import com.broadblog.entity.Tag;
import com.broadblog.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
} 