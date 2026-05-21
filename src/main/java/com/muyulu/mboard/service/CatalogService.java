package com.muyulu.mboard.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyulu.mboard.common.security.AuthContext;
import com.muyulu.mboard.entity.Album;
import com.muyulu.mboard.entity.AlbumComment;
import com.muyulu.mboard.entity.Artist;
import com.muyulu.mboard.entity.Song;
import com.muyulu.mboard.mapper.AlbumCommentMapper;
import com.muyulu.mboard.mapper.AlbumMapper;
import com.muyulu.mboard.mapper.ArtistMapper;
import com.muyulu.mboard.mapper.SongMapper;
import com.muyulu.mboard.service.dto.AlbumQueryRequest;
import com.muyulu.mboard.view.AlbumCardView;
import com.muyulu.mboard.view.AlbumDetailStaticView;
import com.muyulu.mboard.view.AlbumDetailView;
import com.muyulu.mboard.view.CommentView;
import com.muyulu.mboard.view.GenreOptionView;
import com.muyulu.mboard.view.PagedView;
import com.muyulu.mboard.view.SongStaticView;
import com.muyulu.mboard.view.SongView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogService {

    private final AlbumMapper albumMapper;
    private final ArtistMapper artistMapper;
    private final SongMapper songMapper;
    private final AlbumCommentMapper albumCommentMapper;
    private final RatingService ratingService;
    private final RatingRedisService ratingRedisService;
    private final AlbumSearchService albumSearchService;
    private final AlbumDetailCacheService albumDetailCacheService;
    private final GenreCatalogService genreCatalogService;

    public PagedView<AlbumCardView> pageAlbums(AlbumQueryRequest request) {
        try {
            var searchResult = albumSearchService.search(request);
            if (searchResult.albumIds().isEmpty()) {
                return PagedView.<AlbumCardView>builder()
                        .page(request.getPage())
                        .size(request.getSize())
                        .total(0)
                        .records(List.of())
                        .build();
            }
            Map<Long, Album> albumMap = albumMapper.selectBatchIds(searchResult.albumIds()).stream()
                    .collect(Collectors.toMap(Album::getId, Function.identity()));
            List<AlbumCardView> records = searchResult.albumIds().stream()
                    .map(albumMap::get)
                    .filter(album -> album != null && album.isPublished())
                    .map(this::toAlbumCard)
                    .toList();
            return PagedView.<AlbumCardView>builder()
                    .page(request.getPage())
                    .size(request.getSize())
                    .total(searchResult.total())
                    .records(records)
                    .build();
        } catch (Exception ex) {
            log.warn("Album paging fallback to MySQL because Elasticsearch query failed", ex);
            return pageAlbumsFromDb(request);
        }
    }

    private PagedView<AlbumCardView> pageAlbumsFromDb(AlbumQueryRequest request) {
        LambdaQueryWrapper<Album> queryWrapper = new LambdaQueryWrapper<Album>()
                .eq(Album::isPublished, true)
                .orderByDesc(Album::isTrending)
                .orderByDesc(Album::getCreatedAt);
        if (request.getGenre() != null && !request.getGenre().isBlank()) {
            queryWrapper.apply("FIND_IN_SET({0}, genre) > 0", normalizeGenreCode(request.getGenre()));
        }
        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            queryWrapper.like(Album::getName, request.getKeyword());
        }
        Page<Album> pageQuery = new Page<>(request.getPage(), request.getSize());
        Page<Album> result = albumMapper.selectPage(pageQuery, queryWrapper);
        List<AlbumCardView> records = result.getRecords().stream()
                .map(this::toAlbumCard)
                .toList();
        return PagedView.<AlbumCardView>builder()
                .page(result.getCurrent())
                .size(result.getSize())
                .total(result.getTotal())
                .records(records)
                .build();
    }

    public AlbumDetailView albumDetail(Long albumId) {
        AlbumDetailStaticView staticView = albumDetailCacheService.get(albumId);
        if (staticView == null) {
            staticView = buildStaticDetail(albumId);
        }
        if (staticView == null) {
            return null;
        }
        ratingRedisService.increaseAlbumHot(albumId, 0.2D);
        Long userId = AuthContext.requireUserId();
        var snapshot = ratingService.albumSnapshot(albumId);
        Integer currentUserStar = userId == null ? null : ratingService.currentAlbumStar(albumId, userId);
        List<SongView> songs = staticView.getSongs().stream()
                .map(song -> {
                    var songSnapshot = ratingService.songSnapshot(song.getId());
                    Integer userSongStar = userId == null ? null : ratingService.currentSongStar(song.getId(), userId);
                    return SongView.builder()
                            .id(song.getId())
                            .name(song.getName())
                            .trackNo(song.getTrackNo())
                            .durationSeconds(song.getDurationSeconds())
                            .ratingCount(songSnapshot == null ? 0 : (int) songSnapshot.count())
                            .ratingScore10(songSnapshot == null ? null : score10(songSnapshot.avg()))
                            .currentUserStar(userSongStar)
                            .currentUserScore10(userSongStar == null ? null : userSongStar * 2)
                            .build();
                })
                .toList();
        List<CommentView> comments = albumCommentMapper.selectList(new LambdaQueryWrapper<AlbumComment>()
                        .eq(AlbumComment::getAlbumId, albumId)
                        .orderByDesc(AlbumComment::getCreatedAt)
                        .last("limit 20"))
                .stream()
                .map(comment -> CommentView.builder()
                        .id(comment.getId())
                        .username(comment.getUsername())
                        .content(comment.getContent())
                        .ratingStarSnapshot(comment.getRatingStarSnapshot())
                        .ratingScoreSnapshot(comment.getRatingScoreSnapshot())
                        .createdAt(comment.getCreatedAt())
                        .build())
                .toList();
        return AlbumDetailView.builder()
                .id(staticView.getId())
                .artistId(staticView.getArtistId())
                .artistName(staticView.getArtistName())
                .name(staticView.getName())
                .genreCodes(staticView.getGenreCodes())
                .genreLabels(staticView.getGenreLabels())
                .coverUrl(staticView.getCoverUrl())
                .summary(staticView.getSummary())
                .releaseDate(staticView.getReleaseDate())
                .ratingCount(snapshot == null ? 0 : (int) snapshot.count())
                .ratingScore10(snapshot == null ? null : score10(snapshot.avg()))
                .currentUserStar(currentUserStar)
                .currentUserScore10(currentUserStar == null ? null : currentUserStar * 2)
                .songs(songs)
                .comments(comments)
                .build();
    }

    public List<AlbumCardView> topHotAlbums(int limit) {
        List<Long> hotIds = ratingRedisService.topAlbumIds(limit);
        if (!hotIds.isEmpty()) {
            Map<Long, Album> albumMap = albumMapper.selectBatchIds(hotIds).stream()
                    .filter(Album::isPublished)
                    .collect(Collectors.toMap(Album::getId, Function.identity()));
            return hotIds.stream()
                    .map(albumMap::get)
                    .filter(album -> album != null)
                    .map(this::toAlbumCard)
                    .limit(limit)
                    .toList();
        }
        return albumMapper.selectList(new LambdaQueryWrapper<Album>()
                        .eq(Album::isPublished, true))
                .stream()
                .map(this::toAlbumCard)
                .sorted(Comparator.comparing((AlbumCardView view) -> view.getRatingCount() == null ? 0 : view.getRatingCount()).reversed())
                .limit(limit)
                .toList();
    }

    public List<AlbumCardView> trendingAlbums(int limit) {
        return albumMapper.selectList(new LambdaQueryWrapper<Album>()
                        .eq(Album::isPublished, true)
                        .eq(Album::isTrending, true)
                        .orderByDesc(Album::getCreatedAt)
                        .last("limit " + limit))
                .stream()
                .map(this::toAlbumCard)
                .toList();
    }

    public List<AlbumCardView> recommendedAlbums(int limit) {
        return albumMapper.selectList(new LambdaQueryWrapper<Album>()
                        .eq(Album::isPublished, true)
                        .orderByDesc(Album::isTrending)
                        .orderByDesc(Album::getCreatedAt)
                        .last("limit " + limit))
                .stream()
                .map(this::toAlbumCard)
                .toList();
    }

    public List<GenreOptionView> genres() {
        return genreCatalogService.genres();
    }

    public AlbumCardView toAlbumCardPublic(Album album) {
        return toAlbumCard(album);
    }

    private AlbumCardView toAlbumCard(Album album) {
        Artist artist = artistMapper.selectById(album.getArtistId());
        var snapshot = ratingService.albumSnapshot(album.getId());
        List<String> genreCodes = genreCatalogService.parseCodes(album.getGenre());
        return AlbumCardView.builder()
                .id(album.getId())
                .artistId(album.getArtistId())
                .artistName(artist == null ? "Unknown Artist" : artist.getName())
                .name(album.getName())
                .genreCodes(genreCodes)
                .genreLabels(genreCatalogService.labelsByCodes(genreCodes))
                .coverUrl(album.getCoverUrl())
                .releaseDate(album.getReleaseDate() == null ? null : album.getReleaseDate().toString())
                .ratingCount(snapshot == null ? 0 : (int) snapshot.count())
                .ratingScore10(snapshot == null ? null : score10(snapshot.avg()))
                .build();
    }

    private Double score10(double avg) {
        return ratingRedisService.score10(avg).doubleValue();
    }

    private AlbumDetailStaticView buildStaticDetail(Long albumId) {
        Album album = albumMapper.selectById(albumId);
        if (album == null || !album.isPublished()) {
            return null;
        }
        Artist artist = artistMapper.selectById(album.getArtistId());
        List<String> genreCodes = genreCatalogService.parseCodes(album.getGenre());
        List<SongStaticView> songs = songMapper.selectList(new LambdaQueryWrapper<Song>()
                        .eq(Song::getAlbumId, albumId)
                        .orderByAsc(Song::getTrackNo))
                .stream()
                .map(song -> SongStaticView.builder()
                        .id(song.getId())
                        .name(song.getName())
                        .trackNo(song.getTrackNo())
                        .durationSeconds(song.getDurationSeconds())
                        .build())
                .toList();
        AlbumDetailStaticView detail = AlbumDetailStaticView.builder()
                .id(album.getId())
                .artistId(artist == null ? null : artist.getId())
                .artistName(artist == null ? "Unknown Artist" : artist.getName())
                .name(album.getName())
                .genreCodes(genreCodes)
                .genreLabels(genreCatalogService.labelsByCodes(genreCodes))
                .coverUrl(album.getCoverUrl())
                .summary(album.getSummary())
                .releaseDate(album.getReleaseDate() == null ? null : album.getReleaseDate().toString())
                .songs(songs)
                .build();
        albumDetailCacheService.put(albumId, detail);
        return detail;
    }

    private String normalizeGenreCode(String genreCode) {
        return genreCatalogService.normalizeCodes(List.of(genreCode)).getFirst();
    }
}
