package com.jippy.driver.dto.google;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleRoute {

    private Long distanceMeters;

    private String duration;
}