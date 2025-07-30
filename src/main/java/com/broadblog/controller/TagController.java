package com.broadblog.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.broadblog.dto.TagDTO;
import com.broadblog.entity.Tag;
import com.broadblog.mapper.TagMapper;
import com.broadblog.service.TagService;
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
} 