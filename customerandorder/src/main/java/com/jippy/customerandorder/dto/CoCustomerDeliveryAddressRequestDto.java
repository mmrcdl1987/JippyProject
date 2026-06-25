package com.jippy.customerandorder.dto;


import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CoCustomerDeliveryAddressRequestDto {

    @NotNull(message = "Customer Id is required")
    private Integer customerId;


    @DecimalMin(value = "-90.0", message = "Latitude must be greater than or equal to -90")
    @DecimalMax(value = "90.0", message = "Latitude must be less than or equal to 90")
    @NotNull(message = "Latitude is required")
    private Double latitude;

    @DecimalMin(value = "-180.0", message = "Longitude must be greater than or equal to -180")
    @DecimalMax(value = "180.0", message = "Longitude must be less than or equal to 180")
    @NotNull(message = "Longitude is required")
    private Double longitude;

    @NotBlank(message = "Door No is required")
    private String doorNo;

    @NotBlank(message = "Building Name is required")
    private String buildingName;

    @NotBlank(message = "Lane No is required")
    private String laneNo;

//    @NotNull(message = "Area is required")--//changed for production
    private Integer area;

//    @NotNull(message = "City is required")--//changed for production
    private Integer city;

    @NotNull(message = "Created By is required")
    private Integer createdBy;
}