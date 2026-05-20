package com.jippy.driver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
//used for feign client used from address table columns Feilds
public class DriverAddressRequestDto {
    private Integer addressId;

    private Integer jippyAddressId;

    private String addressType;

    // Address fields
    @NotBlank(message = "Building number is required")
    @Size(max = 50)
    private String buildingNumber;

    @NotBlank(message = "Road is required")
    @Size(max = 100)
    private String road;

    @NotBlank(message = "Landmark is required")
    @Size(max = 150)
    private String landmark;

    @NotNull(message = "City id is required")
    private Integer cityId;

    @NotNull(message = "State id is required")
    private Integer stateId;

    @NotNull(message = "Area id is required")
    private Integer areaId;
}
