package com.muyulu.mboard.service;

import com.muyulu.mboard.enums.AlbumGenreType;
import com.muyulu.mboard.view.GenreOptionView;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
public class GenreCatalogService {

    private final List<GenreOptionView> cachedGenres = Arrays.stream(AlbumGenreType.values())
            .map(item -> GenreOptionView.builder()
                    .code(item.getCode())
                    .label(item.getLabel())
                    .build())
            .toList();

    public List<GenreOptionView> genres() {
        return cachedGenres;
    }

    public List<String> normalizeCodes(List<String> codes) {
        if (codes == null) {
            return List.of();
        }
        return codes.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(code -> !code.isBlank())
                .map(code -> AlbumGenreType.fromCode(code).getCode())
                .distinct()
                .toList();
    }

    public List<String> parseCodes(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return List.of();
        }
        return normalizeCodes(Arrays.stream(rawValue.split(",")).toList());
    }

    public String joinCodes(List<String> codes) {
        return String.join(",", normalizeCodes(codes));
    }

    public List<String> labelsByCodes(List<String> codes) {
        return normalizeCodes(codes).stream()
                .map(code -> AlbumGenreType.fromCode(code).getLabel())
                .toList();
    }
}
