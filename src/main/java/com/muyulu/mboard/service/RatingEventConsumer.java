package com.muyulu.mboard.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.muyulu.mboard.common.config.KafkaTopicsProperties;
import com.muyulu.mboard.entity.AlbumRatingAggregate;
import com.muyulu.mboard.entity.AlbumRatingUser;
import com.muyulu.mboard.entity.RatingEventConsumeLog;
import com.muyulu.mboard.entity.SongRatingAggregate;
import com.muyulu.mboard.entity.SongRatingUser;
import com.muyulu.mboard.enums.TargetType;
import com.muyulu.mboard.event.RatingChangedEvent;
import com.muyulu.mboard.mapper.AlbumRatingAggregateMapper;
import com.muyulu.mboard.mapper.AlbumRatingUserMapper;
import com.muyulu.mboard.mapper.RatingEventConsumeLogMapper;
import com.muyulu.mboard.mapper.SongRatingAggregateMapper;
import com.muyulu.mboard.mapper.SongRatingUserMapper;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class RatingEventConsumer {

    private final AlbumRatingUserMapper albumRatingUserMapper;
    private final SongRatingUserMapper songRatingUserMapper;
    private final AlbumRatingAggregateMapper albumRatingAggregateMapper;
    private final SongRatingAggregateMapper songRatingAggregateMapper;
    private final RatingEventConsumeLogMapper consumeLogMapper;
    private final KafkaTopicsProperties topics;

    @KafkaListener(topics = "${app.kafka.topics.album-rating}")
    @Transactional
    public void onAlbumRating(ConsumerRecord<String, RatingChangedEvent> record, Acknowledgment acknowledgment) {
        persist(record.value(), acknowledgment);
    }

    @KafkaListener(topics = "${app.kafka.topics.song-rating}")
    @Transactional
    public void onSongRating(ConsumerRecord<String, RatingChangedEvent> record, Acknowledgment acknowledgment) {
        persist(record.value(), acknowledgment);
    }

    private void persist(RatingChangedEvent event, Acknowledgment acknowledgment) {
        if (consumeLogMapper.selectById(event.getEventId()) != null) {
            acknowledgment.acknowledge();
            return;
        }
        if (event.getTargetType() == TargetType.ALBUM) {
            AlbumRatingUser userRating = albumRatingUserMapper.selectOne(new LambdaQueryWrapper<AlbumRatingUser>()
                    .eq(AlbumRatingUser::getAlbumId, event.getTargetId())
                    .eq(AlbumRatingUser::getUserId, event.getUserId()));
            if (userRating == null) {
                userRating = new AlbumRatingUser();
                userRating.setAlbumId(event.getTargetId());
                userRating.setUserId(event.getUserId());
                userRating.setStar(event.getNewStar());
                albumRatingUserMapper.insert(userRating);
            } else {
                userRating.setStar(event.getNewStar());
                albumRatingUserMapper.updateById(userRating);
            }
            AlbumRatingAggregate aggregate = new AlbumRatingAggregate();
            aggregate.setAlbumId(event.getTargetId());
            aggregate.setRatingCount(event.getRatingCount());
            aggregate.setRatingStarSum(event.getRatingStarSum());
            aggregate.setRatingStarAvg(BigDecimal.valueOf(event.getRatingStarAvg()));
            if (albumRatingAggregateMapper.selectById(event.getTargetId()) == null) {
                albumRatingAggregateMapper.insert(aggregate);
            } else {
                albumRatingAggregateMapper.updateById(aggregate);
            }
        } else {
            SongRatingUser userRating = songRatingUserMapper.selectOne(new LambdaQueryWrapper<SongRatingUser>()
                    .eq(SongRatingUser::getSongId, event.getTargetId())
                    .eq(SongRatingUser::getUserId, event.getUserId()));
            if (userRating == null) {
                userRating = new SongRatingUser();
                userRating.setSongId(event.getTargetId());
                userRating.setUserId(event.getUserId());
                userRating.setStar(event.getNewStar());
                songRatingUserMapper.insert(userRating);
            } else {
                userRating.setStar(event.getNewStar());
                songRatingUserMapper.updateById(userRating);
            }
            SongRatingAggregate aggregate = new SongRatingAggregate();
            aggregate.setSongId(event.getTargetId());
            aggregate.setRatingCount(event.getRatingCount());
            aggregate.setRatingStarSum(event.getRatingStarSum());
            aggregate.setRatingStarAvg(BigDecimal.valueOf(event.getRatingStarAvg()));
            if (songRatingAggregateMapper.selectById(event.getTargetId()) == null) {
                songRatingAggregateMapper.insert(aggregate);
            } else {
                songRatingAggregateMapper.updateById(aggregate);
            }
        }
        RatingEventConsumeLog consumeLog = new RatingEventConsumeLog();
        consumeLog.setEventId(event.getEventId());
        consumeLog.setTopic(event.getTargetType() == TargetType.ALBUM ? topics.getAlbumRating() : topics.getSongRating());
        consumeLogMapper.insert(consumeLog);
        acknowledgment.acknowledge();
    }
}
