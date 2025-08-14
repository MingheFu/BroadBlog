package com.broadblog.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.broadblog.search.PostDocument;

@RestController
@RequestMapping("/api/elasticsearch")
public class ElasticsearchController {

    private final ElasticsearchOperations elasticsearchOperations;

    @Autowired
    public ElasticsearchController(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    // 检查ES连接状态
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 检查posts索引是否存在
            boolean indexExists = elasticsearchOperations.indexOps(IndexCoordinates.of("posts")).exists();
            
            result.put("status", "ok");
            result.put("connected", true);
            result.put("posts_index_exists", indexExists);
            
            if (indexExists) {
                // 获取索引信息
                var indexOps = elasticsearchOperations.indexOps(PostDocument.class);
                result.put("index_name", "posts");
                result.put("index_status", "active");
            }
            
        } catch (Exception e) {
            result.put("status", "error");
            result.put("connected", false);
            result.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    // 获取索引统计信息
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getIndexStats() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            var indexOps = elasticsearchOperations.indexOps(PostDocument.class);
            boolean exists = indexOps.exists();
            
            if (exists) {
                result.put("document_count", "available");
                result.put("index_name", "posts");
                result.put("status", "active");
            } else {
                result.put("status", "index_not_found");
                result.put("document_count", 0);
            }
            
        } catch (Exception e) {
            result.put("status", "error");
            result.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
}
