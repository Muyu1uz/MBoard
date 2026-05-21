package com.muyulu.mboard.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.muyulu.mboard.common.exception.BusinessException;
import com.muyulu.mboard.entity.Album;
import com.muyulu.mboard.entity.Artist;
import com.muyulu.mboard.entity.Song;
import com.muyulu.mboard.mapper.AlbumMapper;
import com.muyulu.mboard.mapper.ArtistMapper;
import com.muyulu.mboard.mapper.SongMapper;
import com.muyulu.mboard.service.dto.AlbumSaveRequest;
import com.muyulu.mboard.service.dto.ArtistSaveRequest;
import com.muyulu.mboard.service.dto.SongSaveRequest;
import com.muyulu.mboard.view.AdminDashboardView;
import com.muyulu.mboard.view.AlbumAdminView;
import com.muyulu.mboard.view.ArtistAdminView;
import com.muyulu.mboard.view.SongAdminView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ArtistMapper artistMapper;
    private final AlbumMapper albumMapper;
    private final SongMapper songMapper;
    private final AlbumSearchService albumSearchService;
    private final AlbumDetailCacheService albumDetailCacheService;
    private final GenreCatalogService genreCatalogService;

    public AdminDashboardView dashboard() {
        List<ArtistAdminView> artists = artistMapper.selectList(null).stream()
                .map(artist -> ArtistAdminView.builder()
                        .id(artist.getId())
                        .name(artist.getName())
                        .avatarUrl(artist.getAvatarUrl())
                        .bio(artist.getBio())
                        .build())
                .toList();
        List<AlbumAdminView> albums = albumMapper.selectList(null).stream()
                .map(this::toAlbumAdminView)
                .toList();
        List<SongAdminView> songs = songMapper.selectList(null).stream()
                .map(song -> SongAdminView.builder()
                        .id(song.getId())
                        .albumId(song.getAlbumId())
                        .name(song.getName())
                        .trackNo(song.getTrackNo())
                        .durationSeconds(song.getDurationSeconds())
                        .build())
                .toList();
        return AdminDashboardView.builder()
                .artists(artists)
                .albums(albums)
                .songs(songs)
                .genres(genreCatalogService.genres())
                .build();
    }

    public ArtistAdminView saveArtist(ArtistSaveRequest request) {
        Artist artist = new Artist();
        artist.setName(request.getName());
        artist.setAvatarUrl(request.getAvatarUrl());
        artist.setBio(request.getBio());
        artistMapper.insert(artist);
        return ArtistAdminView.builder()
                .id(artist.getId())
                .name(artist.getName())
                .avatarUrl(artist.getAvatarUrl())
                .bio(artist.getBio())
                .build();
    }

    public AlbumAdminView saveAlbum(AlbumSaveRequest request) {
        Artist artist = artistMapper.selectById(request.getArtistId());
        if (artist == null) {
            throw new BusinessException("Artist does not exist");
        }
        List<String> genreCodes = genreCatalogService.normalizeCodes(request.getGenres());
        if (genreCodes.isEmpty()) {
            throw new BusinessException("At least one genre is required");
        }
        Album album = new Album();
        album.setArtistId(request.getArtistId());
        album.setName(request.getName());
        album.setGenre(genreCatalogService.joinCodes(genreCodes));
        album.setCoverUrl(request.getCoverUrl());
        album.setSummary(request.getSummary());
        album.setReleaseDate(request.getReleaseDate());
        album.setTrending(Boolean.TRUE.equals(request.getTrending()));
        album.setPublished(!Boolean.FALSE.equals(request.getPublished()));
        albumMapper.insert(album);
        albumSearchService.reindexAlbum(album);
        albumDetailCacheService.evict(album.getId());
        return toAlbumAdminView(album);
    }

    public SongAdminView saveSong(SongSaveRequest request) {
        Album album = albumMapper.selectById(request.getAlbumId());
        if (album == null) {
            throw new BusinessException("Album does not exist");
        }
        Song song = new Song();
        song.setAlbumId(request.getAlbumId());
        song.setName(request.getName());
        song.setTrackNo(request.getTrackNo());
        song.setDurationSeconds(request.getDurationSeconds());
        songMapper.insert(song);
        albumDetailCacheService.evict(song.getAlbumId());
        return SongAdminView.builder()
                .id(song.getId())
                .albumId(song.getAlbumId())
                .name(song.getName())
                .trackNo(song.getTrackNo())
                .durationSeconds(song.getDurationSeconds())
                .build();
    }

    public List<Album> albumsByArtist(Long artistId) {
        return albumMapper.selectList(new LambdaQueryWrapper<Album>().eq(Album::getArtistId, artistId));
    }

    private AlbumAdminView toAlbumAdminView(Album album) {
        List<String> genreCodes = genreCatalogService.parseCodes(album.getGenre());
        return AlbumAdminView.builder()
                .id(album.getId())
                .artistId(album.getArtistId())
                .name(album.getName())
                .genreCodes(genreCodes)
                .genreLabels(genreCatalogService.labelsByCodes(genreCodes))
                .coverUrl(album.getCoverUrl())
                .summary(album.getSummary())
                .releaseDate(album.getReleaseDate() == null ? null : album.getReleaseDate().toString())
                .trending(album.isTrending())
                .published(album.isPublished())
                .build();
    }
}
