package com.muyulu.mboard.controller;

import com.muyulu.mboard.common.api.ApiResponse;
import com.muyulu.mboard.service.ArtistService;
import com.muyulu.mboard.view.ArtistDetailView;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/artists")
@RequiredArgsConstructor
public class ArtistController {

    private final ArtistService artistService;

    @GetMapping("/{artistId}")
    public ApiResponse<ArtistDetailView> detail(@PathVariable Long artistId) {
        return ApiResponse.success(artistService.detail(artistId));
    }
}
