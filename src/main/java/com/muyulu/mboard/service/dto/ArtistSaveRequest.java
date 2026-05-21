package com.muyulu.mboard.service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ArtistSaveRequest {

    @NotBlank(message = "Artist name is required")
    private String name;
    private String avatarUrl;
    private String bio;
}
