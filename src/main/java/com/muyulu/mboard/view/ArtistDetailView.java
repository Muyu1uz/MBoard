package com.muyulu.mboard.view;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ArtistDetailView {

    private Long id;
    private String name;
    private String avatarUrl;
    private String bio;
    private List<AlbumCardView> albums;
}
