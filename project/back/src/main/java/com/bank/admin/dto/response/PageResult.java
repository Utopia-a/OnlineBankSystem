package com.bank.admin.dto.response;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class PageResult<T> {

    private List<T> records;
    private long total;
    private int page;
    private int size;
    private int totalPages;

    public static <T> PageResult<T> of(Page<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setRecords(page.getContent());
        result.setTotal(page.getTotalElements());
        result.setPage(page.getNumber() + 1);
        result.setSize(page.getSize());
        result.setTotalPages(page.getTotalPages());
        return result;
    }
}
