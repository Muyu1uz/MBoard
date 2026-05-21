package com.muyulu.mboard.controller;

import com.muyulu.mboard.common.api.ApiResponse;
import com.muyulu.mboard.service.CatalogService;
import com.muyulu.mboard.service.CommentService;
import com.muyulu.mboard.service.RatingService;
import com.muyulu.mboard.service.dto.AlbumQueryRequest;
import com.muyulu.mboard.service.dto.CommentRequest;
import com.muyulu.mboard.service.dto.RateRequest;
import com.muyulu.mboard.view.AlbumDetailView;
import com.muyulu.mboard.view.CommentView;
import com.muyulu.mboard.view.PagedView;
import com.muyulu.mboard.view.RatingView;
import com.muyulu.mboard.view.AlbumCardView;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/albums")
@RequiredArgsConstructor
public class AlbumController {

    private final CatalogService catalogService;
    private final RatingService ratingService;
    private final CommentService commentService;

    @GetMapping
    public ApiResponse<PagedView<AlbumCardView>> page(@RequestParam(defaultValue = "1") long page,
                                                      @RequestParam(defaultValue = "12") long size,
                                                      @RequestParam(required = false) String genre,
                                                      @RequestParam(required = false) String keyword) {
        AlbumQueryRequest request = new AlbumQueryRequest();
        request.setPage(page);
        request.setSize(size);
        request.setGenre(genre);
        request.setKeyword(keyword);
        return ApiResponse.success(catalogService.pageAlbums(request));
    }

    @GetMapping("/{albumId}")
    public ApiResponse<AlbumDetailView> detail(@PathVariable Long albumId) {
        return ApiResponse.success(catalogService.albumDetail(albumId));
    }

    @PostMapping("/{albumId}/ratings")
    public ApiResponse<RatingView> rateAlbum(@PathVariable Long albumId, @Valid @RequestBody RateRequest request) {
        return ApiResponse.success("Rated", ratingService.rateAlbum(albumId, request.getStar()));
    }

    @PostMapping("/{albumId}/comments")
    public ApiResponse<CommentView> createComment(@PathVariable Long albumId, @Valid @RequestBody CommentRequest request) {
        return ApiResponse.success("Commented", commentService.createAlbumComment(albumId, request));
    }
}
