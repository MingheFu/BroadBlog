package com.broadblog.search;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface PostSearchRepository extends ElasticsearchRepository<PostDocument, Long> {
    Page<PostDocument> findByTitleContainingOrContentContaining(String title, String content, Pageable pageable);
}


