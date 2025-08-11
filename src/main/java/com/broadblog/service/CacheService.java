package com.broadblog.service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class CacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 缓存键前缀常量
    public static final String POST_CACHE_PREFIX = "post:";
    public static final String USER_CACHE_PREFIX = "user:";
    public static final String TAG_CACHE_PREFIX = "tag:";
    public static final String HOT_POSTS_CACHE_KEY = "hot:posts";
    public static final String POPULAR_TAGS_CACHE_KEY = "popular:tags";
    public static final String TAG_CLOUD_CACHE_KEY = "tag:cloud";
    public static final String USER_STATS_CACHE_PREFIX = "stats:user:";
    public static final String SYSTEM_STATS_CACHE_KEY = "stats:system";

    /**
     * 设置缓存
     */
    public void set(String key, Object value, Duration duration) {
        redisTemplate.opsForValue().set(key, value, duration);
    }

    /**
     * 设置缓存（默认过期时间）
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value, Duration.ofHours(1));
    }

    /**
     * 获取缓存
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key, Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value != null && clazz.isInstance(value)) {
            return Optional.of((T) value);
        }
        return Optional.empty();
    }

    /**
     * 删除缓存
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 批量删除缓存
     */
    public void delete(Set<String> keys) {
        redisTemplate.delete(keys);
    }

    /**
     * 检查键是否存在
     */
    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 设置过期时间
     */
    public void expire(String key, Duration duration) {
        redisTemplate.expire(key, duration);
    }

    /**
     * 获取过期时间
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 递增计数器
     */
    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    /**
     * 递增计数器（指定增量）
     */
    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 添加到有序集合
     */
    public void zAdd(String key, Object value, double score) {
        redisTemplate.opsForZSet().add(key, value, score);
    }

    /**
     * 获取有序集合的成员（按分数降序）
     */
    public Set<Object> zRevRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRange(key, start, end);
    }



    /**
     * 添加到列表
     */
    public void lPush(String key, Object value) {
        redisTemplate.opsForList().leftPush(key, value);
    }

    /**
     * 获取列表范围
     */
    public List<Object> lRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    /**
     * 设置哈希字段
     */
    public void hSet(String key, String field, Object value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    /**
     * 获取哈希字段
     */
    public Object hGet(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }

    /**
     * 获取所有哈希字段
     */
    public Object hGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 添加到集合
     */
    public void sAdd(String key, Object value) {
        redisTemplate.opsForSet().add(key, value);
    }

    /**
     * 从集合中移除
     */
    public void sRem(String key, Object value) {
        redisTemplate.opsForSet().remove(key, value);
    }

    /**
     * 检查集合中是否包含某个值
     */
    public boolean sIsMember(String key, Object value) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, value));
    }

    /**
     * 获取集合的大小
     */
    public Long sCard(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    /**
     * 获取集合的所有成员
     */
    public Set<Object> sMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 获取集合的随机成员
     */
    public Object sRandomMember(String key) {
        return redisTemplate.opsForSet().randomMember(key);
    }

    /**
     * 清除所有缓存（谨慎使用）
     */
    public void clearAll() {
        Set<String> keys = redisTemplate.keys("*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 清除指定前缀的缓存
     */
    public void clearByPrefix(String prefix) {
        Set<String> keys = redisTemplate.keys(prefix + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
