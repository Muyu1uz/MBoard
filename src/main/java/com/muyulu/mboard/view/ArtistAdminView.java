package com.muyulu.mboard.view;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ArtistAdminView {

    private Long id;
    private String name;
    private String avatarUrl;
    private String bio;
}
