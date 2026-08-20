package com.jippy.foodandmart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BannerInfoDto {

    private String bannerType;  // e.g., "MAIN", "BEST_RESTAURANT", "DEALS"
    private String bannerUrl;   // e.g., "https://s3.../banner.jpg"
    private Integer slotNumber; // e.g., 1, 2, 3
    private Integer outletId;
    private String outletName;
}
