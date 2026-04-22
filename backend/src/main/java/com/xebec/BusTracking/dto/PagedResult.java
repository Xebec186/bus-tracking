package com.xebec.BusTracking.dto;


import lombok.Getter;

import java.util.List;

/**
 * Thin wrapper around a paginated result set.
 * Replaces Spring's Page<T> at the controller/view boundary so that
 * templates never depend on Spring Data internals.
 */
@Getter
public class PagedResult<T> {

    private final List<T> content;
    private final int     currentPage;    // 0-based
    private final int     totalPages;
    private final long    totalElements;
    private final int     pageSize;

    public PagedResult(List<T> content,
                       int     currentPage,
                       int     totalPages,
                       long    totalElements,
                       int     pageSize) {
        this.content       = content;
        this.currentPage   = currentPage;
        this.totalPages    = totalPages;
        this.totalElements = totalElements;
        this.pageSize      = pageSize;
    }

    /** True when a previous page exists */
    public boolean hasPrevious() {
        return currentPage > 0;
    }

    /** True when a next page exists */
    public boolean hasNext() {
        return currentPage < totalPages - 1;
    }

    /** 1-based start index of the current page – convenient for templates */
    public long getFirstElement() {
        return (long) currentPage * pageSize + 1;
    }

    /** 1-based end index of the current page – convenient for templates */
    public long getLastElement() {
        long end = (long) currentPage * pageSize + content.size();
        return Math.min(end, totalElements);
    }

    /**
     * Factory – build a PagedResult directly from a Spring Data Page<T>
     * after mapping the content.
     *
     * Usage in a service impl:
     *
     *   Page<User> page = userRepository.findFiltered(..., pageable);
     *   List<UserListItemDto> mapped = page.getContent().stream()
     *           .map(this::toListItem).toList();
     *   return PagedResult.of(mapped, page);
     */
    public static <T> PagedResult<T> of(List<T> mappedContent,
                                        org.springframework.data.domain.Page<?> page) {
        return new PagedResult<>(
                mappedContent,
                page.getNumber(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.getSize()
        );
    }
}