package com.muyulu.mboard.controller;

import com.muyulu.mboard.common.api.ApiResponse;
import com.muyulu.mboard.service.AdminService;
import com.muyulu.mboard.service.UploadService;
import com.muyulu.mboard.service.dto.AlbumSaveRequest;
import com.muyulu.mboard.service.dto.ArtistSaveRequest;
import com.muyulu.mboard.service.dto.SongSaveRequest;
import com.muyulu.mboard.view.AdminDashboardView;
import com.muyulu.mboard.view.AlbumAdminView;
import com.muyulu.mboard.view.ArtistAdminView;
import com.muyulu.mboard.view.SongAdminView;
import com.muyulu.mboard.view.UploadView;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final UploadService uploadService;

    @GetMapping("/dashboard")
    public ApiResponse<AdminDashboardView> dashboard() {
        return ApiResponse.success(adminService.dashboard());
    }

    @PostMapping("/artists")
    public ApiResponse<ArtistAdminView> saveArtist(@Valid @RequestBody ArtistSaveRequest request) {
        return ApiResponse.success(adminService.saveArtist(request));
    }

    @PostMapping("/albums")
    public ApiResponse<AlbumAdminView> saveAlbum(@Valid @RequestBody AlbumSaveRequest request) {
        return ApiResponse.success(adminService.saveAlbum(request));
    }

    @PostMapping("/songs")
    public ApiResponse<SongAdminView> saveSong(@Valid @RequestBody SongSaveRequest request) {
        return ApiResponse.success(adminService.saveSong(request));
    }

    @PostMapping("/uploads/images")
    public ApiResponse<UploadView> uploadImage(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success(UploadView.builder()
                .url(uploadService.saveImage(file))
                .build());
    }
}
