package com.jippy.driver.dto.routing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteResult {

    private BigDecimal distanceKm;

    private Long distanceMeters;

    private Long durationSeconds;
}