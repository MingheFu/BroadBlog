package com.broadblog.config;

import java.util.concurrent.Callable;

import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.lang.Nullable;

/**
 * A two-level cache that checks Caffeine (L1) first, then Redis (L2).
 * On L2 hits, it back-fills L1 to speed up subsequent reads.
 */
public class TwoLevelCache implements Cache {

    private final String cacheName;
    private final Cache l1CaffeineCache;
    private final Cache l2RedisCache;

    public TwoLevelCache(String cacheName, Cache l1CaffeineCache, Cache l2RedisCache) {
        this.cacheName = cacheName;
        this.l1CaffeineCache = l1CaffeineCache;
        this.l2RedisCache = l2RedisCache;
    }

    @Override
    public String getName() {
        return cacheName;
    }

    @Override
    public Object getNativeCache() {
        return this; // not exposing internals
    }

    @Override
    @Nullable
    public ValueWrapper get(Object key) {
        // 1) Try L1
        ValueWrapper l1 = l1CaffeineCache.get(key);
        if (l1 != null) {
            return l1;
        }
        // 2) Try L2
        ValueWrapper l2 = l2RedisCache.get(key);
        if (l2 != null) {
            // back-fill L1
            l1CaffeineCache.put(key, l2.get());
            return new SimpleValueWrapper(l2.get());
        }
        return null;
    }

    @Override
    @Nullable
    public <T> T get(Object key, @Nullable Class<T> type) {
        T l1 = l1CaffeineCache.get(key, type);
        if (l1 != null) {
            return l1;
        }
        T l2 = l2RedisCache.get(key, type);
        if (l2 != null) {
            l1CaffeineCache.put(key, l2);
            return l2;
        }
        return null;
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        // First try L1, then L2; if both miss, load and write-through
        ValueWrapper l1 = l1CaffeineCache.get(key);
        if (l1 != null) {
            @SuppressWarnings("unchecked")
            T value = (T) l1.get();
            return value;
        }
        ValueWrapper l2 = l2RedisCache.get(key);
        if (l2 != null) {
            @SuppressWarnings("unchecked")
            T value = (T) l2.get();
            l1CaffeineCache.put(key, value);
            return value;
        }
        try {
            T loaded = valueLoader.call();
            // write-through to both caches
            put(key, loaded);
            return loaded;
        } catch (Exception ex) {
            throw new ValueRetrievalException(key, valueLoader, ex);
        }
    }

    @Override
    public void put(Object key, @Nullable Object value) {
        l1CaffeineCache.put(key, value);
        l2RedisCache.put(key, value);
    }

    @Override
    @Nullable
    public ValueWrapper putIfAbsent(Object key, @Nullable Object value) {
        ValueWrapper existing = get(key);
        if (existing == null) {
            put(key, value);
            return null;
        }
        return existing;
    }

    @Override
    public void evict(Object key) {
        l1CaffeineCache.evict(key);
        l2RedisCache.evict(key);
    }

    @Override
    public void clear() {
        l1CaffeineCache.clear();
        l2RedisCache.clear();
    }
}


