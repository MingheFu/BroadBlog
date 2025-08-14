package com.broadblog.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;

import com.broadblog.search.PostDocument;

@Configuration
public class ElasticsearchConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchConfig.class);
    
    @Bean
    public CommandLineRunner elasticsearchIndexInitializer(ElasticsearchOperations elasticsearchOperations) {
        return args -> {
            try {
                initializePostIndex(elasticsearchOperations);
            } catch (Exception e) {
                logger.error("Failed to initialize Elasticsearch index", e);
            }
        };
    }
    
    private void initializePostIndex(ElasticsearchOperations elasticsearchOperations) {
        try {
            // 检查索引是否存在
            boolean indexExists = elasticsearchOperations.indexOps(IndexCoordinates.of("posts")).exists();
            
            if (!indexExists) {
                // 创建索引
                elasticsearchOperations.indexOps(PostDocument.class).create();
                logger.info("Created Elasticsearch index 'posts'");
                
                // 创建映射
                elasticsearchOperations.indexOps(PostDocument.class).putMapping();
                logger.info("Created Elasticsearch mapping for 'posts'");
            } else {
                // 更新映射（如果索引已存在）
                elasticsearchOperations.indexOps(PostDocument.class).putMapping();
                logger.info("Updated Elasticsearch mapping for 'posts'");
            }
            
            // 验证索引状态
            var indexOps = elasticsearchOperations.indexOps(PostDocument.class);
            logger.info("Index 'posts' initialized successfully");
            
        } catch (Exception e) {
            logger.error("Error during Elasticsearch index initialization", e);
            throw e;
        }
    }
}
