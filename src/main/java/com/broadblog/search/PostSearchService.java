package com.broadblog.search;

import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.stereotype.Service;

import com.broadblog.entity.Post;
import com.broadblog.entity.Tag;

@Service
public class PostSearchService {
    private static final Logger logger = LoggerFactory.getLogger(PostSearchService.class);
    
    private final PostSearchRepository postSearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    @Autowired
    public PostSearchService(PostSearchRepository postSearchRepository, ElasticsearchOperations elasticsearchOperations) {
        this.postSearchRepository = postSearchRepository;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    public void indexPost(Post post) {
        try {
            PostDocument doc = mapToDocument(post);
            postSearchRepository.save(doc);
            logger.debug("Successfully indexed post: {}", post.getId());
        } catch (Exception e) {
            logger.error("Failed to index post {}: {}", post.getId(), e.getMessage());
            throw new RuntimeException("Failed to index post to Elasticsearch", e);
        }
    }

    public void deletePost(Long postId) {
        try {
            postSearchRepository.deleteById(postId);
            logger.debug("Successfully deleted post from index: {}", postId);
        } catch (Exception e) {
            logger.error("Failed to delete post {} from index: {}", postId, e.getMessage());
            throw new RuntimeException("Failed to delete post from Elasticsearch", e);
        }
    }

    public void indexPosts(List<Post> posts) {
        try {
            List<PostDocument> docs = posts.stream()
                .map(this::mapToDocument)
                .collect(Collectors.toList());
            postSearchRepository.saveAll(docs);
            logger.info("Successfully indexed {} posts", posts.size());
        } catch (Exception e) {
            logger.error("Failed to index posts: {}", e.getMessage());
            throw new RuntimeException("Failed to index posts to Elasticsearch", e);
        }
    }

    public Page<PostDocument> search(String keyword, int page, int size) {
        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 10;
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        try {
            // 使用ElasticsearchOperations直接执行查询
            String queryString = String.format(
                "{\"multi_match\": {\"query\": \"%s\", \"fields\": [\"title^2\", \"content\"]}}",
                keyword
            );
            Query query = new StringQuery(queryString, pageable);
            
            SearchHits<PostDocument> searchHits = elasticsearchOperations.search(query, PostDocument.class);
            
            List<PostDocument> content = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
            
            return new PageImpl<>(content, pageable, searchHits.getTotalHits());
            
        } catch (Exception e) {
            logger.error("Failed to search posts with keyword '{}': {}", keyword, e.getMessage());
            // 降级到Repository查询
            return postSearchRepository.searchByKeyword(keyword, pageable);
        }
    }
    
    public Page<PostDocument> searchByTag(String tagName, int page, int size) {
        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 10;
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        try {
            String queryString = String.format(
                "{\"term\": {\"tagNames\": \"%s\"}}",
                tagName
            );
            Query query = new StringQuery(queryString, pageable);
            
            SearchHits<PostDocument> searchHits = elasticsearchOperations.search(query, PostDocument.class);
            
            List<PostDocument> content = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
            
            return new PageImpl<>(content, pageable, searchHits.getTotalHits());
            
        } catch (Exception e) {
            logger.error("Failed to search posts by tag '{}': {}", tagName, e.getMessage());
            return postSearchRepository.findByTagName(tagName, pageable);
        }
    }
    
    public Page<PostDocument> searchByAuthor(Long authorId, int page, int size) {
        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 10;
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        try {
            String queryString = String.format(
                "{\"term\": {\"authorId\": %d}}",
                authorId
            );
            Query query = new StringQuery(queryString, pageable);
            
            SearchHits<PostDocument> searchHits = elasticsearchOperations.search(query, PostDocument.class);
            
            List<PostDocument> content = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
            
            return new PageImpl<>(content, pageable, searchHits.getTotalHits());
            
        } catch (Exception e) {
            logger.error("Failed to search posts by author {}: {}", authorId, e.getMessage());
            return postSearchRepository.findByAuthorId(authorId, pageable);
        }
    }

    private PostDocument mapToDocument(Post post) {
        PostDocument doc = new PostDocument();
        doc.setId(post.getId());
        doc.setTitle(post.getTitle());
        doc.setContent(post.getContent());
        doc.setAuthorId(post.getAuthor() != null ? post.getAuthor().getId() : null);
        if (post.getTags() != null) {
            doc.setTagNames(post.getTags().stream().map(Tag::getName).collect(Collectors.toList()));
        }
        if (post.getCreatedAt() != null) {
            doc.setCreatedAt(post.getCreatedAt().toInstant(ZoneOffset.UTC));
        }
        if (post.getUpdatedAt() != null) {
            doc.setUpdatedAt(post.getUpdatedAt().toInstant(ZoneOffset.UTC));
        }
        return doc;
    }
}


