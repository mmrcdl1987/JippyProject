package com.jippy.driver.service;

import com.jippy.driver.dto.google.GoogleRoutesRequest;
import com.jippy.driver.dto.google.GoogleRoutesResponse;

public interface GoogleRoutesClient {

    GoogleRoutesResponse calculateRoute(
            GoogleRoutesRequest request
    );
}