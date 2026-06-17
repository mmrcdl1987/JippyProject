package com.jippy.customerandorder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SmsCountryRequestDto {

    @JsonProperty("Text")
    private String text;

    @JsonProperty("Number")
    private String number;

    @JsonProperty("SenderId")
    private String senderId;
}