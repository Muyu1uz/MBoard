package com.muyulu.mboard.view;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AlbumDetailStaticView {

    private Long id;
    private Long artistId;
    private String artistName;
    private String name;
    private List<String> genreCodes;
    private List<String> genreLabels;
    private String coverUrl;
    private String summary;
    private String releaseDate;
    private List<SongStaticView> songs;
}
