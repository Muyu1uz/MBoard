package com.muyulu.mboard.view;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PagedView<T> {

    private long page;
    private long size;
    private long total;
    private List<T> records;
}
