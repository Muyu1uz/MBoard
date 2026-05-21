package com.muyulu.mboard.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.muyulu.mboard.entity.Album;
import com.muyulu.mboard.entity.Artist;
import com.muyulu.mboard.mapper.AlbumMapper;
import com.muyulu.mboard.mapper.ArtistMapper;
import com.muyulu.mboard.view.AlbumCardView;
import com.muyulu.mboard.view.ArtistDetailView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArtistService {

    private final ArtistMapper artistMapper;
    private final AlbumMapper albumMapper;
    private final CatalogService catalogService;

    public ArtistDetailView detail(Long artistId) {
        Artist artist = artistMapper.selectById(artistId);
        if (artist == null) {
            return null;
        }
        List<AlbumCardView> albums = albumMapper.selectList(new LambdaQueryWrapper<Album>()
                        .eq(Album::getArtistId, artistId)
                        .eq(Album::isPublished, true)
                        .orderByDesc(Album::getReleaseDate))
                .stream()
                .map(catalogService::toAlbumCardPublic)
                .toList();
        return ArtistDetailView.builder()
                .id(artist.getId())
                .name(artist.getName())
                .avatarUrl(artist.getAvatarUrl())
                .bio(artist.getBio())
                .albums(albums)
                .build();
    }
}
