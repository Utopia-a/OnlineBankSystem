package com.bank.admin.dto.request;

import lombok.Data;

@Data
public class PageRequest {

    private int page = 1;
    private int size = 10;

    public org.springframework.data.domain.PageRequest toSpringPageRequest() {
        return org.springframework.data.domain.PageRequest.of(page - 1, size);
    }

    public org.springframework.data.domain.PageRequest toSpringPageRequest(org.springframework.data.domain.Sort sort) {
        return org.springframework.data.domain.PageRequest.of(page - 1, size, sort);
    }
}
