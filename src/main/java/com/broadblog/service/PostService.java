package com.broadblog.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.broadblog.entity.Post;
import com.broadblog.entity.Tag;
import com.broadblog.entity.User;
import com.broadblog.repository.PostRepository;
import com.broadblog.repository.UserRepository;
import com.broadblog.search.PostDocument;
import com.broadblog.search.PostSearchService;

@Service
public class PostService {

    private static final Logger logger = LoggerFactory.getLogger(PostService.class);

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final TagService tagService;
    private final CacheService cacheService;
    private final PostSearchService postSearchService;

    @Autowired
    public PostService(PostRepository postRepository, UserRepository userRepository, 
                      TagService tagService, CacheService cacheService, PostSearchService postSearchService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.tagService = tagService;
        this.cacheService = cacheService;
        this.postSearchService = postSearchService;
    }

    // Create or update a post
    @CacheEvict(value = {"posts", "hotPosts", "searchResults"}, allEntries = true)
    public Post savePost(Post post) {
        if (post.getAuthor() != null && post.getAuthor().getId() != null) {
            User author = userRepository.findById(post.getAuthor().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
            post.setAuthor(author);
        } else {
            throw new RuntimeException("Author is required");
        }
        
        // 处理标签使用计数
        Set<Tag> oldTags = new HashSet<>();
        if (post.getId() != null) {
            // 更新帖子：获取原有标签
            Optional<Post> existingPost = postRepository.findById(post.getId());
            if (existingPost.isPresent() && existingPost.get().getTags() != null) {
                oldTags = new HashSet<>(existingPost.get().getTags());
            }
        }
        
        Post savedPost = postRepository.save(post);
        // 同步到 Elasticsearch 索引
        try {
            postSearchService.indexPost(savedPost);
        } catch (Exception e) {
            System.err.println("Failed to index post to Elasticsearch: " + e.getMessage());
        }
        
        // 更新标签使用计数
        updateTagUsageCounts(oldTags, savedPost.getTags() != null ? new HashSet<>(savedPost.getTags()) : new HashSet<>());
        
        // 清除相关缓存
        clearPostRelatedCache(savedPost.getId());
        
        return savedPost;
    }

    // Get all posts
    @Cacheable(value = "posts", key = "'all'")
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    // Get a post by ID
    @Cacheable(value = "posts", key = "#id")
    public Optional<Post> getPostById(Long id) {
        // 增加阅读量统计
        incrementPostViews(id);
        return postRepository.findById(id);
    }

    // Delete a post by ID
    @CacheEvict(value = {"posts", "hotPosts", "searchResults"}, allEntries = true)
    public void deletePost(Long id) {
        // 获取要删除的帖子的标签，减少使用计数
        Optional<Post> postToDelete = postRepository.findById(id);
        if (postToDelete.isPresent() && postToDelete.get().getTags() != null) {
            for (Tag tag : postToDelete.get().getTags()) {
                tagService.decrementTagUsage(tag.getId());
            }
        }
        
        postRepository.deleteById(id);
        // 从 Elasticsearch 删除
        try {
            postSearchService.deletePost(id);
        } catch (Exception e) {
            System.err.println("Failed to delete post from Elasticsearch: " + e.getMessage());
        }
        
        // 清除相关缓存
        clearPostRelatedCache(id);
    }

    // Custom: Get posts by author
    @Cacheable(value = "posts", key = "'author:' + #authorId")
    public List<Post> getPostsByAuthorId(Long authorId) {
        return postRepository.findByAuthorId(authorId);
    }

    // 分页获取帖子（带缓存）
    @Cacheable(value = "posts", key = "'page:' + #page + ':' + #size")
    public Page<Post> getPostsByPage(int page, int size) {
        // 验证分页参数
        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 10;
        
        // 按创建时间倒序排列
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return postRepository.findAll(pageable);
    }
    
    // 根据用户ID分页获取帖子（带缓存）
    @Cacheable(value = "posts", key = "'authorPage:' + #authorId + ':' + #page + ':' + #size")
    public Page<Post> getPostsByAuthorIdWithPage(Long authorId, int page, int size) {
        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 10;
        
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return postRepository.findByAuthorId(authorId, pageable);
    }
    
    // 搜索帖子（综合搜索：标题、内容、标签）- 带缓存
    @Cacheable(value = "searchResults", key = "'search:' + #keyword + ':' + #page + ':' + #size")
    public Page<Post> searchPosts(String keyword, int page, int size) {
        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 10;
        
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        // 如果关键词为空，返回所有帖子
        if (keyword == null || keyword.trim().isEmpty()) {
            return postRepository.findAll(pageable);
        }
        
        return postRepository.searchPosts(keyword.trim(), pageable);
    }

    // 使用 Elasticsearch 进行全文搜索
    @Cacheable(value = "searchResults", key = "'es:' + #keyword + ':' + #page + ':' + #size")
    @Transactional(readOnly = true)
    public Page<Post> searchPostsEs(String keyword, int page, int size) {
        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 10;
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        if (keyword == null || keyword.trim().isEmpty()) {
            return postRepository.findAll(pageable);
        }

        try {
            // 获取ES搜索结果
            Page<PostDocument> docPage = postSearchService.search(keyword.trim(), page, size);
            
            if (docPage.getContent().isEmpty()) {
                return new PageImpl<>(List.of(), pageable, 0);
            }
            
            // 只获取ID列表，避免序列化问题
            List<Long> ids = docPage.getContent().stream()
                .map(PostDocument::getId)
                .collect(Collectors.toList());
            
            // 使用JOIN FETCH预加载关联数据，避免懒加载问题
            List<Post> posts = postRepository.findByIdInWithAuthorAndTags(ids);
            
            // 按ES结果顺序重新排列
            Map<Long, Post> postMap = posts.stream()
                .collect(Collectors.toMap(Post::getId, post -> post));
            
            List<Post> orderedPosts = ids.stream()
                .map(id -> postMap.get(id))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
            
            return new PageImpl<>(orderedPosts, pageable, docPage.getTotalElements());
            
        } catch (Exception e) {
            // 如果ES搜索失败，降级到数据库搜索
            logger.warn("ES search failed for keyword '{}', falling back to database search: {}", keyword, e.getMessage());
            return searchPosts(keyword, page, size);
        }
    }
    
    // 使用 Elasticsearch 按标签搜索
    @Cacheable(value = "searchResults", key = "'es_tag:' + #tagName + ':' + #page + ':' + #size")
    @Transactional(readOnly = true)
    public Page<Post> searchPostsByTagEs(String tagName, int page, int size) {
        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 10;
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        if (tagName == null || tagName.trim().isEmpty()) {
            return postRepository.findAll(pageable);
        }

        try {
            Page<PostDocument> docPage = postSearchService.searchByTag(tagName.trim(), page, size);
            
            if (docPage.getContent().isEmpty()) {
                return new PageImpl<>(List.of(), pageable, 0);
            }
            
            List<Long> ids = docPage.getContent().stream()
                .map(PostDocument::getId)
                .collect(Collectors.toList());
            
            List<Post> posts = postRepository.findByIdInWithAuthorAndTags(ids);
            
            Map<Long, Post> postMap = posts.stream()
                .collect(Collectors.toMap(Post::getId, post -> post));
            
            List<Post> orderedPosts = ids.stream()
                .map(id -> postMap.get(id))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
            
            return new PageImpl<>(orderedPosts, pageable, docPage.getTotalElements());
            
        } catch (Exception e) {
            logger.warn("ES tag search failed for tag '{}', falling back to database search: {}", tagName, e.getMessage());
            return searchByTag(tagName, page, size);
        }
    }
    
    // 使用 Elasticsearch 按作者搜索
    @Cacheable(value = "searchResults", key = "'es_author:' + #authorId + ':' + #page + ':' + #size")
    @Transactional(readOnly = true)
    public Page<Post> searchPostsByAuthorEs(Long authorId, int page, int size) {
        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 10;
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        if (authorId == null) {
            return postRepository.findAll(pageable);
        }

        try {
            Page<PostDocument> docPage = postSearchService.searchByAuthor(authorId, page, size);
            
            if (docPage.getContent().isEmpty()) {
                return new PageImpl<>(List.of(), pageable, 0);
            }
            
            List<Long> ids = docPage.getContent().stream()
                .map(PostDocument::getId)
                .collect(Collectors.toList());
            
            List<Post> posts = postRepository.findByIdInWithAuthorAndTags(ids);
            
            Map<Long, Post> postMap = posts.stream()
                .collect(Collectors.toMap(Post::getId, post -> post));
            
            List<Post> orderedPosts = ids.stream()
                .map(id -> postMap.get(id))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
            
            return new PageImpl<>(orderedPosts, pageable, docPage.getTotalElements());
            
        } catch (Exception e) {
            logger.warn("ES author search failed for author {}, falling back to database search: {}", authorId, e.getMessage());
            return getPostsByAuthorIdWithPage(authorId, page, size);
        }
    }

    // 重新构建全部 Elasticsearch 索引
    @CacheEvict(value = {"searchResults"}, allEntries = true)
    public void reindexAllPosts() {
        List<Post> posts = postRepository.findAll();
        postSearchService.indexPosts(posts);
    }
    
    // 按标题搜索 - 带缓存
    @Cacheable(value = "searchResults", key = "'title:' + #title + ':' + #page + ':' + #size")
    public Page<Post> searchByTitle(String title, int page, int size) {
        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 10;
        
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return postRepository.findByTitleContainingIgnoreCase(title, pageable);
    }
    
    // 按内容搜索 - 带缓存
    @Cacheable(value = "searchResults", key = "'content:' + #content + ':' + #page + ':' + #size")
    public Page<Post> searchByContent(String content, int page, int size) {
        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 10;
        
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return postRepository.findByContentContainingIgnoreCase(content, pageable);
    }
    
    // 按标题或内容搜索（复合搜索）- 带缓存
    @Cacheable(value = "searchResults", key = "'titleContent:' + #keyword + ':' + #page + ':' + #size")
    public Page<Post> searchByTitleOrContent(String keyword, int page, int size) {
        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 10;
        
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return postRepository.findByTitleOrContentContaining(keyword, pageable);
    }
    
    // 按标签搜索 - 带缓存
    @Cacheable(value = "searchResults", key = "'tag:' + #tagName + ':' + #page + ':' + #size")
    public Page<Post> searchByTag(String tagName, int page, int size) {
        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 10;
        
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return postRepository.findByTagsNameContaining(tagName, pageable);
    }

    public List<Post> getHotPosts(int top) {
        String hotPostsKey = "hot_posts";
        
        // 从Redis获取热门文章ID列表
        Set<Object> hotPostIds = cacheService.zRevRange(hotPostsKey, 0, top - 1);
        
        if (hotPostIds == null || hotPostIds.isEmpty()) {
            // 缓存中没有数据，从数据库获取并构建缓存
            return buildHotPostsCache(top);
        }
        
        // 根据ID列表获取文章详情，使用JOIN FETCH避免懒加载
        List<Long> ids = hotPostIds.stream()
            .map(id -> Long.valueOf(id.toString()))
            .collect(Collectors.toList());
        
        try {
            return postRepository.findByIdInWithAuthorAndTags(ids);
        } catch (Exception e) {
            logger.warn("Failed to get hot posts with JOIN FETCH, falling back to simple query: {}", e.getMessage());
            // 降级到简单查询
            return hotPostIds.stream()
                .map(id -> postRepository.findById(Long.valueOf(id.toString())))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        }
    }
    
    /**
     * 构建热门文章缓存
     * 从数据库获取文章，按阅读量排序，存入Redis
     */
    private List<Post> buildHotPostsCache(int top) {
        // 这里需要先实现阅读量统计，暂时返回空列表
        // TODO: 实现阅读量统计后，这里可以按阅读量排序
        List<Post> posts = postRepository.findAll();
        
        // 暂时按创建时间排序，取最新的文章
        List<Post> topPosts = posts.stream()
            .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
            .limit(top)
            .collect(Collectors.toList());
        
        // 将结果存入Redis缓存
        String hotPostsKey = "hot_posts";
        for (Post post : topPosts) {
            // 暂时使用创建时间作为分数，后续可以改为阅读量
            double score = post.getCreatedAt().toEpochSecond(java.time.ZoneOffset.UTC);
            cacheService.zAdd(hotPostsKey, post.getId().toString(), score);
        }
        
        // 设置缓存过期时间
        cacheService.expire(hotPostsKey, java.time.Duration.ofHours(6));
        
        return topPosts;
    }
    
    /**
     * 增加文章阅读量
     * 使用Redis计数器实现
     */
    public void incrementPostViews(Long postId) {
        String viewKey = "post:" + postId + ":views";
        String hotPostsKey = "hot_posts";
        
        // 增加阅读量
        Long views = cacheService.increment(viewKey);
        
        // 更新热门文章排行
        cacheService.zAdd(hotPostsKey, postId.toString(), views.doubleValue());
        
        // 设置阅读量缓存的过期时间（30天）
        cacheService.expire(viewKey, java.time.Duration.ofDays(30));
        
        // 设置热门文章排行的过期时间（6小时）
        cacheService.expire(hotPostsKey, java.time.Duration.ofHours(6));
        
        System.out.println("post " + postId + " views increased to: " + views);
    }
    
    /**
     * 获取文章阅读量
     */
    public Long getPostViews(Long postId) {
        String viewKey = "post:" + postId + ":views";
        Optional<Long> views = cacheService.get(viewKey, Long.class);
        return views.orElse(0L);
    }

    /**
     * 清除文章相关的缓存
     */
    private void clearPostRelatedCache(Long postId) {
        // 清除单个文章缓存
        cacheService.delete("posts::" + postId);
        
        // 清除分页缓存（因为文章数量可能发生变化）
        cacheService.clearByPrefix("posts::page:");
        cacheService.clearByPrefix("posts::authorPage:");
        
        // 清除搜索缓存（因为文章内容可能发生变化）
        cacheService.clearByPrefix("searchResults::");
        
        // 清除热门文章排行缓存
        cacheService.delete("hot_posts");
        
        System.out.println("post " + postId + " related cache cleared");
    }
    
    // 辅助方法：更新标签使用计数
    private void updateTagUsageCounts(Set<Tag> oldTags, Set<Tag> newTags) {
        // 找出被移除的标签，减少使用计数
        for (Tag oldTag : oldTags) {
            if (!newTags.contains(oldTag)) {
                tagService.decrementTagUsage(oldTag.getId());
            }
        }
        
        // 找出新添加的标签，增加使用计数
        for (Tag newTag : newTags) {
            if (!oldTags.contains(newTag)) {
                tagService.incrementTagUsage(newTag.getId());
            }
        }
    }
}