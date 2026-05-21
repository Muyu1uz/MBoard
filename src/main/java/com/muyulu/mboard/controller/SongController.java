package com.muyulu.mboard.controller;

import com.muyulu.mboard.common.api.ApiResponse;
import com.muyulu.mboard.service.RatingService;
import com.muyulu.mboard.service.dto.RateRequest;
import com.muyulu.mboard.view.RatingView;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
public class SongController {

    private final RatingService ratingService;

    @PostMapping("/{songId}/ratings")
    public ApiResponse<RatingView> rateSong(@PathVariable Long songId, @Valid @RequestBody RateRequest request) {
        return ApiResponse.success("Rated", ratingService.rateSong(songId, request.getStar()));
    }
}
