package com.banking.report.util;

import com.banking.report.dto.TransactionQueryRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

/**
 * 分页工具类
 */
public class PageUtil {

    private PageUtil() {}

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "amount");
    private static final Set<String> ALLOWED_SORT_DIRS   = Set.of("ASC", "DESC");

    /**
     * 从请求参数构建 Pageable，防止非法排序字段注入
     */
    public static Pageable buildPageable(TransactionQueryRequest req) {
        String sortField = ALLOWED_SORT_FIELDS.contains(req.getSortBy())
                ? req.getSortBy() : "createdAt";
        String sortDir = ALLOWED_SORT_DIRS.contains(
                req.getSortDir() != null ? req.getSortDir().toUpperCase() : "DESC")
                ? req.getSortDir().toUpperCase() : "DESC";

        Sort sort = "ASC".equals(sortDir)
                ? Sort.by(sortField).ascending()
                : Sort.by(sortField).descending();

        return PageRequest.of(req.getPage(), req.getSize(), sort);
    }
}
