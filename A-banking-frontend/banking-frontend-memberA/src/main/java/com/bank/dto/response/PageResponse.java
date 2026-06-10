package com.bank.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 分页响应 DTO
 */
@Data
@Schema(description = "分页响应数据")
public class PageResponse<T> {

    @Schema(description = "当前页数据列表")
    private List<T> records;

    @Schema(description = "总记录数", example = "100")
    private long total;

    @Schema(description = "当前页码（从1开始）", example = "1")
    private int page;

    @Schema(description = "每页大小", example = "10")
    private int pageSize;

    @Schema(description = "总页数", example = "10")
    private int totalPages;

    public static <T> PageResponse<T> of(List<T> records, long total, int page, int pageSize) {
        PageResponse<T> pr = new PageResponse<>();
        pr.records = records;
        pr.total = total;
        pr.page = page;
        pr.pageSize = pageSize;
        pr.totalPages = (int) Math.ceil((double) total / pageSize);
        return pr;
    }
}
