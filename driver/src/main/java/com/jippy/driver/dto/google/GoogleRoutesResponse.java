package com.jippy.driver.dto.google;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleRoutesResponse {

    private List<GoogleRoute> routes;
}