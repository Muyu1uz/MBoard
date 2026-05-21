package com.muyulu.mboard.service;

import com.muyulu.mboard.common.config.AlbumCacheProperties;
import com.muyulu.mboard.view.AlbumDetailStaticView;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AlbumDetailCacheService {

    private static final String CACHE_NAME = "albumDetailCache";
    private static final String REDIS_PREFIX = "album:detail:v2:";

    private final CacheManager cacheManager;
    private final RedisTemplate<String, Object> objectRedisTemplate;
    private final AlbumCacheProperties cacheProperties;

    public AlbumDetailStaticView get(Long albumId) {
        var cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            AlbumDetailStaticView caffeineValue = cache.get(albumId, AlbumDetailStaticView.class);
            if (caffeineValue != null) {
                return caffeineValue;
            }
        }
        Object redisValue = objectRedisTemplate.opsForValue().get(redisKey(albumId));
        if (redisValue instanceof AlbumDetailStaticView detailView) {
            putCaffeine(albumId, detailView);
            return detailView;
        }
        return null;
    }

    public void put(Long albumId, AlbumDetailStaticView detailView) {
        objectRedisTemplate.opsForValue().set(
                redisKey(albumId),
                detailView,
                cacheProperties.getAlbumDetailRedisTtl().toMillis(),
                TimeUnit.MILLISECONDS
        );
        putCaffeine(albumId, detailView);
    }

    public void evict(Long albumId) {
        var cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            cache.evict(albumId);
        }
        objectRedisTemplate.delete(redisKey(albumId));
    }

    private void putCaffeine(Long albumId, AlbumDetailStaticView detailView) {
        var cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            cache.put(albumId, detailView);
        }
    }

    private String redisKey(Long albumId) {
        return REDIS_PREFIX + albumId;
    }
}
