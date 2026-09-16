package com.jippy.driver.dto.uber;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationDto {

    private Double latitude;
    private Double longitude;
}
