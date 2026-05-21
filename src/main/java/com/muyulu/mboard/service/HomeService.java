package com.muyulu.mboard.service;

import com.muyulu.mboard.view.HomeView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final CatalogService catalogService;

    public HomeView home() {
        return HomeView.builder()
                .recommendedAlbums(catalogService.recommendedAlbums(8))
                .trendingAlbums(catalogService.trendingAlbums(8))
                .topAlbums(catalogService.topHotAlbums(5))
                .genres(catalogService.genres())
                .build();
    }
}
