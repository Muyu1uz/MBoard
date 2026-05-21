package com.muyulu.mboard.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SongSaveRequest {

    @NotNull(message = "Album is required")
    private Long albumId;

    @NotBlank(message = "Song name is required")
    private String name;

    private Integer trackNo;
    private Integer durationSeconds;
}
