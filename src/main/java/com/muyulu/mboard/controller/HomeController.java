package com.muyulu.mboard.controller;

import com.muyulu.mboard.common.api.ApiResponse;
import com.muyulu.mboard.service.HomeService;
import com.muyulu.mboard.view.HomeView;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    @GetMapping
    public ApiResponse<HomeView> home() {
        return ApiResponse.success(homeService.home());
    }
}
