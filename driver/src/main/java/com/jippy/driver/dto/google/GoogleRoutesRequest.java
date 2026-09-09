package com.jippy.driver.dto.google;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleRoutesRequest {

    private GoogleLocation origin;

    private GoogleLocation destination;

    private String travelMode;

    private String routingPreference;
}