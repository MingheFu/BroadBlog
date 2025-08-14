package com.broadblog.search;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface PostSearchRepository extends ElasticsearchRepository<PostDocument, Long> {
    
    // 多字段搜索，使用简单查询语法
    @Query("{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"title^2\", \"content\"]}}")
    Page<PostDocument> searchByKeyword(String keyword, Pageable pageable);
    
    // 按标签搜索
    @Query("{\"term\": {\"tagNames\": \"?0\"}}")
    Page<PostDocument> findByTagName(String tagName, Pageable pageable);
    
    // 按作者搜索
    @Query("{\"term\": {\"authorId\": ?0}}")
    Page<PostDocument> findByAuthorId(Long authorId, Pageable pageable);
}


