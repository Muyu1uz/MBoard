package com.muyulu.mboard.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class AlbumSaveRequest {

    @NotNull(message = "Artist is required")
    private Long artistId;

    @NotBlank(message = "Album name is required")
    private String name;

    @NotNull(message = "Genres are required")
    private List<String> genres;
    private String coverUrl;
    private String summary;
    private LocalDate releaseDate;
    private Boolean trending = false;
    private Boolean published = true;
}
