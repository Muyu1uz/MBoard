package com.muyulu.mboard.service;

import com.muyulu.mboard.common.exception.BusinessException;
import com.muyulu.mboard.enums.TargetType;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RatingRedisService {

    private static final String HOT_RANK_KEY = "album:hot:zset";

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<List> ratingUpdateScript;
    private final DefaultRedisScript<List> ratingRollbackScript;

    public RatingMutationResult mutate(TargetType targetType, Long targetId, Long userId, Integer newStar) {
        String userHashKey = userKey(targetType, targetId);
        String aggregateKey = aggregateKey(targetType, targetId);
        List<?> result = redisTemplate.execute(
                ratingUpdateScript,
                List.of(userHashKey, aggregateKey, HOT_RANK_KEY),
                userId.toString(),
                newStar.toString(),
                hotDelta(targetType)
        );
        if (result == null || result.size() < 4) {
            throw new BusinessException("Failed to update rating cache");
        }
        return RatingMutationResult.builder()
                .previousStar(((Number) result.get(0)).intValue())
                .ratingCount(((Number) result.get(1)).longValue())
                .ratingStarSum(((Number) result.get(2)).longValue())
                .ratingStarAvg(((Number) result.get(3)).doubleValue())
                .build();
    }

    public void rollback(TargetType targetType, Long targetId, Long userId, Integer oldStar, Integer newStar) {
        String userHashKey = userKey(targetType, targetId);
        String aggregateKey = aggregateKey(targetType, targetId);
        List<?> result = redisTemplate.execute(
                ratingRollbackScript,
                List.of(userHashKey, aggregateKey, HOT_RANK_KEY),
                userId.toString(),
                String.valueOf(oldStar),
                String.valueOf(newStar),
                hotDelta(targetType)
        );
        if (result == null || result.isEmpty() || ((Number) result.get(0)).intValue() == -1) {
            throw new BusinessException("Failed to rollback rating cache");
        }
    }

    public RatingSnapshot getAlbumRating(Long albumId) {
        return getSnapshot(TargetType.ALBUM, albumId);
    }

    public RatingSnapshot getSongRating(Long songId) {
        return getSnapshot(TargetType.SONG, songId);
    }

    public Integer getUserAlbumStar(Long albumId, Long userId) {
        return getUserStar(TargetType.ALBUM, albumId, userId);
    }

    public Integer getUserSongStar(Long songId, Long userId) {
        return getUserStar(TargetType.SONG, songId, userId);
    }

    public void warmAlbumAggregate(Long albumId, long count, long sum, double avg) {
        warmSnapshot(TargetType.ALBUM, albumId, count, sum, avg);
    }

    public void warmSongAggregate(Long songId, long count, long sum, double avg) {
        warmSnapshot(TargetType.SONG, songId, count, sum, avg);
    }

    public void increaseAlbumHot(Long albumId, double delta) {
        redisTemplate.opsForZSet().incrementScore(HOT_RANK_KEY, albumId.toString(), delta);
    }

    public List<Long> topAlbumIds(int limit) {
        Set<String> ids = redisTemplate.opsForZSet().reverseRange(HOT_RANK_KEY, 0, Math.max(0, limit - 1));
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<Long> result = new ArrayList<>();
        for (String id : ids) {
            result.add(Long.parseLong(id));
        }
        return result;
    }

    private RatingSnapshot getSnapshot(TargetType type, Long id) {
        List<String> values = redisTemplate.opsForHash().multiGet(
                aggregateKey(type, id),
                List.of("count", "sum", "avg")
        ).stream().map(value -> value == null ? null : value.toString()).toList();
        if (values.isEmpty() || values.getFirst() == null) {
            return null;
        }
        long count = Long.parseLong(values.get(0));
        long sum = Long.parseLong(values.get(1));
        double avg = Double.parseDouble(values.get(2));
        return new RatingSnapshot(count, sum, avg);
    }

    private Integer getUserStar(TargetType type, Long id, Long userId) {
        Object star = redisTemplate.opsForHash().get(userKey(type, id), userId.toString());
        if (star == null) {
            return null;
        }
        return Integer.parseInt(star.toString());
    }

    private void warmSnapshot(TargetType type, Long id, long count, long sum, double avg) {
        redisTemplate.opsForHash().putAll(
                aggregateKey(type, id),
                java.util.Map.of(
                        "count", String.valueOf(count),
                        "sum", String.valueOf(sum),
                        "avg", String.valueOf(avg)
                )
        );
    }

    public BigDecimal score10(double avgStar) {
        return BigDecimal.valueOf(avgStar)
                .multiply(BigDecimal.valueOf(2))
                .setScale(1, RoundingMode.HALF_UP);
    }

    private String userKey(TargetType type, Long id) {
        return switch (type) {
            case ALBUM -> "album:rating:user:" + id;
            case SONG -> "song:rating:user:" + id;
        };
    }

    private String aggregateKey(TargetType type, Long id) {
        return switch (type) {
            case ALBUM -> "album:rating:agg:" + id;
            case SONG -> "song:rating:agg:" + id;
        };
    }

    private String hotDelta(TargetType type) {
        return type == TargetType.ALBUM ? "1" : "0";
    }

    @Getter
    @Builder
    public static class RatingMutationResult {
        private Integer previousStar;
        private Long ratingCount;
        private Long ratingStarSum;
        private Double ratingStarAvg;
    }

    public record RatingSnapshot(long count, long sum, double avg) {
    }
}
