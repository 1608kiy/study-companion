package com.studycompanion.vo;

import lombok.Data;

import java.util.List;

@Data
public class PageResponse<T> {
    private List<T> records;
    private long total;
    private int page;
    private int size;
    private int totalPages;

    public PageResponse(List<T> records, long total, int page, int size) {
        this.records = records;
        this.total = total;
        this.page = page;
        this.size = size;
        this.totalPages = (int) Math.ceil((double) total / size);
    }
}
