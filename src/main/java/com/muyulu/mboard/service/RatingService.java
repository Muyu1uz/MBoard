package com.muyulu.mboard.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.muyulu.mboard.common.config.KafkaTopicsProperties;
import com.muyulu.mboard.common.exception.BusinessException;
import com.muyulu.mboard.common.security.AuthContext;
import com.muyulu.mboard.common.security.AuthUser;
import com.muyulu.mboard.entity.Album;
import com.muyulu.mboard.entity.AlbumRatingAggregate;
import com.muyulu.mboard.entity.AlbumRatingUser;
import com.muyulu.mboard.entity.Song;
import com.muyulu.mboard.entity.SongRatingAggregate;
import com.muyulu.mboard.entity.SongRatingUser;
import com.muyulu.mboard.enums.TargetType;
import com.muyulu.mboard.event.RatingChangedEvent;
import com.muyulu.mboard.mapper.AlbumMapper;
import com.muyulu.mboard.mapper.AlbumRatingAggregateMapper;
import com.muyulu.mboard.mapper.AlbumRatingUserMapper;
import com.muyulu.mboard.mapper.SongMapper;
import com.muyulu.mboard.mapper.SongRatingAggregateMapper;
import com.muyulu.mboard.mapper.SongRatingUserMapper;
import com.muyulu.mboard.view.RatingView;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final AlbumMapper albumMapper;
    private final SongMapper songMapper;
    private final AlbumRatingAggregateMapper albumRatingAggregateMapper;
    private final SongRatingAggregateMapper songRatingAggregateMapper;
    private final AlbumRatingUserMapper albumRatingUserMapper;
    private final SongRatingUserMapper songRatingUserMapper;
    private final RatingRedisService ratingRedisService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopicsProperties topicsProperties;

    public RatingView rateAlbum(Long albumId, Integer star) {
        Album album = albumMapper.selectById(albumId);
        if (album == null || !album.isPublished()) {
            throw new BusinessException("Album does not exist");
        }
        return rate(TargetType.ALBUM, albumId, star);
    }

    public RatingView rateSong(Long songId, Integer star) {
        Song song = songMapper.selectById(songId);
        if (song == null) {
            throw new BusinessException("Song does not exist");
        }
        return rate(TargetType.SONG, songId, star);
    }

    public Integer currentAlbumStar(Long albumId, Long userId) {
        Integer cached = ratingRedisService.getUserAlbumStar(albumId, userId);
        if (cached != null) {
            return cached;
        }
        AlbumRatingUser userRating = albumRatingUserMapper.selectOne(new LambdaQueryWrapper<AlbumRatingUser>()
                .eq(AlbumRatingUser::getAlbumId, albumId)
                .eq(AlbumRatingUser::getUserId, userId));
        return userRating == null ? null : userRating.getStar();
    }

    public Integer currentSongStar(Long songId, Long userId) {
        Integer cached = ratingRedisService.getUserSongStar(songId, userId);
        if (cached != null) {
            return cached;
        }
        SongRatingUser userRating = songRatingUserMapper.selectOne(new LambdaQueryWrapper<SongRatingUser>()
                .eq(SongRatingUser::getSongId, songId)
                .eq(SongRatingUser::getUserId, userId));
        return userRating == null ? null : userRating.getStar();
    }

    public RatingRedisService.RatingSnapshot albumSnapshot(Long albumId) {
        RatingRedisService.RatingSnapshot snapshot = ratingRedisService.getAlbumRating(albumId);
        if (snapshot != null) {
            return snapshot;
        }
        AlbumRatingAggregate aggregate = albumRatingAggregateMapper.selectById(albumId);
        if (aggregate == null) {
            return null;
        }
        double avg = aggregate.getRatingStarAvg() == null ? 0 : aggregate.getRatingStarAvg().doubleValue();
        ratingRedisService.warmAlbumAggregate(albumId, aggregate.getRatingCount(), aggregate.getRatingStarSum(), avg);
        return new RatingRedisService.RatingSnapshot(aggregate.getRatingCount(), aggregate.getRatingStarSum(), avg);
    }

    public RatingRedisService.RatingSnapshot songSnapshot(Long songId) {
        RatingRedisService.RatingSnapshot snapshot = ratingRedisService.getSongRating(songId);
        if (snapshot != null) {
            return snapshot;
        }
        SongRatingAggregate aggregate = songRatingAggregateMapper.selectById(songId);
        if (aggregate == null) {
            return null;
        }
        double avg = aggregate.getRatingStarAvg() == null ? 0 : aggregate.getRatingStarAvg().doubleValue();
        ratingRedisService.warmSongAggregate(songId, aggregate.getRatingCount(), aggregate.getRatingStarSum(), avg);
        return new RatingRedisService.RatingSnapshot(aggregate.getRatingCount(), aggregate.getRatingStarSum(), avg);
    }

    private RatingView rate(TargetType targetType, Long targetId, Integer star) {
        AuthUser authUser = AuthContext.get();
        if (authUser == null) {
            throw new BusinessException("Login required");
        }
        RatingRedisService.RatingMutationResult result = ratingRedisService.mutate(targetType, targetId, authUser.getUserId(), star);
        RatingChangedEvent event = RatingChangedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .userId(authUser.getUserId())
                .username(authUser.getUsername())
                .targetType(targetType)
                .targetId(targetId)
                .oldStar(result.getPreviousStar())
                .newStar(star)
                .ratingCount(result.getRatingCount())
                .ratingStarSum(result.getRatingStarSum())
                .ratingStarAvg(result.getRatingStarAvg())
                .eventTime(LocalDateTime.now())
                .build();
        try {
            String topic = targetType == TargetType.ALBUM ? topicsProperties.getAlbumRating() : topicsProperties.getSongRating();
            kafkaTemplate.send(topic, targetId.toString(), event).get();
        } catch (Exception ex) {
            ratingRedisService.rollback(targetType, targetId, authUser.getUserId(), result.getPreviousStar(), star);
            throw new BusinessException("Rating submission failed, please retry");
        }
        return RatingView.builder()
                .previousStar(result.getPreviousStar())
                .currentStar(star)
                .ratingCount(result.getRatingCount().intValue())
                .ratingScore10(ratingRedisService.score10(result.getRatingStarAvg()).doubleValue())
                .build();
    }
}
