package com.muyulu.mboard.view;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AlbumCardView {

    private Long id;
    private Long artistId;
    private String artistName;
    private String name;
    private List<String> genreCodes;
    private List<String> genreLabels;
    private String coverUrl;
    private String releaseDate;
    private Integer ratingCount;
    private Double ratingScore10;
}
