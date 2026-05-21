package com.muyulu.mboard.view;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class HomeView {

    private List<AlbumCardView> recommendedAlbums;
    private List<AlbumCardView> trendingAlbums;
    private List<AlbumCardView> topAlbums;
    private List<GenreOptionView> genres;
}
