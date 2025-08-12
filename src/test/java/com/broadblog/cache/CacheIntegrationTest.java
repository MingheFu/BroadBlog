package com.broadblog.cache;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.broadblog.entity.Post;
import com.broadblog.repository.PostRepository;
import com.broadblog.service.PostService;

@SpringBootTest
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class CacheIntegrationTest {

    // Spin up a Redis container for tests
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @MockBean
    private PostRepository postRepository;

    @Autowired
    private PostService postService;

    @BeforeAll
    void setupRedisProps() {
        String host = redis.getHost();
        Integer port = redis.getMappedPort(6379);
        System.setProperty("spring.data.redis.host", host);
        System.setProperty("spring.data.redis.port", String.valueOf(port));
        // Use database 1 for tests to isolate from local dev
        System.setProperty("spring.data.redis.database", "1");
        // Ensure cache type uses redis (L2) beneath two-level manager
        System.setProperty("spring.cache.type", "redis");
    }

    @Test
    void getPostById_shouldHitCacheOnSecondCall() {
        Post post = new Post();
        post.setId(1L);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        // First call -> miss, repo invoked
        Optional<Post> r1 = postService.getPostById(1L);
        assertThat(r1).isPresent();

        // Second call -> should be cache hit, repo still only once
        Optional<Post> r2 = postService.getPostById(1L);
        assertThat(r2).isPresent();

        verify(postRepository, times(1)).findById(1L);
    }
}


