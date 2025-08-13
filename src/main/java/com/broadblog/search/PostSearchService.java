package com.broadblog.search;

import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.broadblog.entity.Post;
import com.broadblog.entity.Tag;

@Service
public class PostSearchService {
    private final PostSearchRepository postSearchRepository;

    @Autowired
    public PostSearchService(PostSearchRepository postSearchRepository) {
        this.postSearchRepository = postSearchRepository;
    }

    public void indexPost(Post post) {
        PostDocument doc = mapToDocument(post);
        postSearchRepository.save(doc);
    }

    public void deletePost(Long postId) {
        postSearchRepository.deleteById(postId);
    }

    public void indexPosts(List<Post> posts) {
        List<PostDocument> docs = posts.stream().map(this::mapToDocument).collect(Collectors.toList());
        postSearchRepository.saveAll(docs);
    }

    public Page<PostDocument> search(String keyword, int page, int size) {
        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 10;
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return postSearchRepository.findByTitleContainingOrContentContaining(keyword, keyword, pageable);
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


