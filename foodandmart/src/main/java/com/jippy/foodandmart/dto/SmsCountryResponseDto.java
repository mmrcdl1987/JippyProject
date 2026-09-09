package com.jippy.foodandmart.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SmsCountryResponseDto {

    @JsonProperty("messageUUID")
    private String messageUUID;

    private String status;

    private String message;
}