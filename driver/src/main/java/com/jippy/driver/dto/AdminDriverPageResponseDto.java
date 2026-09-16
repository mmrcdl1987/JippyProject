package com.jippy.driver.dto;

import com.jippy.driver.dto.AdminDriverDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDriverPageResponseDto {

    private List<AdminDriverDto> drivers;

    private long totalElements;

    private int totalPages;

    private int currentPage;

    private int pageSize;

    private boolean hasNext;

    private boolean hasPrevious;
}

