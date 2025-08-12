package com.broadblog.config;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

/**
 * A CacheManager that wraps two managers: L1 (Caffeine) and L2 (Redis).
 * It returns a {@link TwoLevelCache} that delegates to the two levels.
 */
public class TwoLevelCacheManager implements CacheManager {

    private final CaffeineCacheManager l1CaffeineManager;
    private final CacheManager l2RedisManager;
    private final Map<String, Cache> cacheByName = new LinkedHashMap<>();

    public TwoLevelCacheManager(CaffeineCacheManager l1CaffeineManager, CacheManager l2RedisManager) {
        this.l1CaffeineManager = l1CaffeineManager;
        this.l2RedisManager = l2RedisManager;
    }

    @Override
    public Cache getCache(String name) {
        return cacheByName.computeIfAbsent(name, n -> {
            Cache l1 = l1CaffeineManager.getCache(n);
            Cache l2 = l2RedisManager.getCache(n);
            if (l1 == null || l2 == null) {
                // Fallback to a simple local cache if anything missing
                ConcurrentMapCacheManager fallback = new ConcurrentMapCacheManager(n);
                return fallback.getCache(n);
            }
            return new TwoLevelCache(n, l1, l2);
        });
    }

    @Override
    public Collection<String> getCacheNames() {
        return Collections.unmodifiableSet(cacheByName.keySet());
    }
}


