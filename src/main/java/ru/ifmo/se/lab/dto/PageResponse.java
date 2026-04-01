package ru.ifmo.se.lab.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageResponse<T> {
    private List<T> content;
    private Integer page;
    private Integer size;
    private Boolean hasNext;
    @JsonAlias("total_elements")
    private Long totalElements;
    @JsonAlias("total_pages")
    private Integer totalPages;
}
