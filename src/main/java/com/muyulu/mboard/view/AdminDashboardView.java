package com.muyulu.mboard.view;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AdminDashboardView {

    private List<ArtistAdminView> artists;
    private List<AlbumAdminView> albums;
    private List<SongAdminView> songs;
    private List<GenreOptionView> genres;
}
