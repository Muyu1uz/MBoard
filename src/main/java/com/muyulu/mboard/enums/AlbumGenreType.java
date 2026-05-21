package com.muyulu.mboard.enums;

import com.muyulu.mboard.common.exception.BusinessException;

import java.util.Arrays;

public enum AlbumGenreType {
    POP("pop", "流行"),
    HIPHOP("hiphop", "嘻哈"),
    ELECTRONIC("electronic", "电子"),
    JAZZ("jazz", "爵士"),
    ROCK("rock", "摇滚"),
    RNB("rnb", "R&B"),
    FOLK("folk", "民谣"),
    CLASSICAL("classical", "古典");

    private final String code;
    private final String label;

    AlbumGenreType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static AlbumGenreType fromCode(String code) {
        return Arrays.stream(values())
                .filter(item -> item.code.equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new BusinessException("Unsupported album genre: " + code));
    }
}
