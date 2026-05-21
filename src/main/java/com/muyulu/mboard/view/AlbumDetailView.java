package com.muyulu.mboard.view;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AlbumDetailView {

    private Long id;
    private Long artistId;
    private String artistName;
    private String name;
    private List<String> genreCodes;
    private List<String> genreLabels;
    private String coverUrl;
    private String summary;
    private String releaseDate;
    private Integer ratingCount;
    private Double ratingScore10;
    private Integer currentUserStar;
    private Integer currentUserScore10;
    private List<SongView> songs;
    private List<CommentView> comments;
}
