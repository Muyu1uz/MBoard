package com.muyulu.mboard.service;

import com.muyulu.mboard.common.config.KafkaTopicsProperties;
import com.muyulu.mboard.common.exception.BusinessException;
import com.muyulu.mboard.common.security.AuthContext;
import com.muyulu.mboard.common.security.AuthUser;
import com.muyulu.mboard.entity.Album;
import com.muyulu.mboard.entity.AlbumComment;
import com.muyulu.mboard.event.AlbumCommentCreatedEvent;
import com.muyulu.mboard.mapper.AlbumCommentMapper;
import com.muyulu.mboard.mapper.AlbumMapper;
import com.muyulu.mboard.service.dto.CommentRequest;
import com.muyulu.mboard.view.CommentView;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final AlbumMapper albumMapper;
    private final AlbumCommentMapper albumCommentMapper;
    private final RatingService ratingService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopicsProperties topicsProperties;

    public CommentView createAlbumComment(Long albumId, CommentRequest request) {
        AuthUser authUser = AuthContext.get();
        if (authUser == null) {
            throw new BusinessException("Login required");
        }
        Album album = albumMapper.selectById(albumId);
        if (album == null || !album.isPublished()) {
            throw new BusinessException("Album does not exist");
        }
        Integer star = ratingService.currentAlbumStar(albumId, authUser.getUserId());
        AlbumComment comment = new AlbumComment();
        comment.setAlbumId(albumId);
        comment.setUserId(authUser.getUserId());
        comment.setUsername(authUser.getUsername());
        comment.setContent(request.getContent());
        comment.setRatingStarSnapshot(star);
        comment.setRatingScoreSnapshot(star == null ? null : star * 2);
        albumCommentMapper.insert(comment);
        kafkaTemplate.send(topicsProperties.getAlbumComment(), albumId.toString(), AlbumCommentCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .albumId(albumId)
                .userId(authUser.getUserId())
                .eventTime(LocalDateTime.now())
                .build());
        return CommentView.builder()
                .id(comment.getId())
                .username(comment.getUsername())
                .content(comment.getContent())
                .ratingStarSnapshot(comment.getRatingStarSnapshot())
                .ratingScoreSnapshot(comment.getRatingScoreSnapshot())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
