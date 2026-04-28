package com.jippy.foodandmart.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

/**
 * Why @JsonIgnoreProperties(ignoreUnknown = true):
 * The frontend and older Postman collections may send fields that were
 * removed from the data model (e.g. "zone", "approver", "subscriptionPlan").
 * Without this annotation Jackson throws UnrecognizedPropertyException and
 * the entire request fails with a 500. Silently ignoring unknown fields is
 * the correct behaviour — the removed fields are simply not persisted.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FmOutletRequestDTO {

    // ── outlets table ─────────────────────────────────────────────────────────

    @NotBlank(message = "Outlet name is required")
    @Size(max = 100, message = "Outlet name must not exceed 100 characters")
    private String outletName;

    @NotNull(message = "Merchant ID is required")
    private Integer merchantId;

    @NotBlank(message = "Cuisine type is required")
    @Size(max = 100, message = "Cuisine type must not exceed 100 characters")
    private String cuisineType;

    @NotBlank(message = "Outlet phone is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Outlet phone must be a valid 10-digit Indian mobile number")
    private String outletPhone;

    // Outlet ID used as the predecessor / login identity
    private String outletId;

    // Outlet password (portal credential)
    private String outletPassword;

    // ── address table ─────────────────────────────────────────────────────────

    @NotBlank(message = "Building number is required")
    @Size(max = 50)
    private String buildingNumber;

    @NotBlank(message = "Road is required")
    @Size(max = 100)
    private String road;

    @Size(max = 150)
    private String landmark;

    @NotNull(message = "City ID is required")
    private Integer cityId;

    /**
     * State name as entered in the XLS sheet (e.g. "Maharashtra").
     * The service will look this up in the states table and resolve the state_id.
     */
    @JsonAlias({"state", "stateName"})
    @NotBlank(message = "State name is required")
    @Size(max = 100)
    private String stateName;

    /**
     * Area name as entered in the upload sheet\'s ZipCode column (e.g. "Banjara Hills").
     *
     * <p>The service resolves this name to the integer area_id FK via the area table.
     * The upload template column is still labelled "ZipCode" for backward compatibility,
     * but its value is now a human-readable area name, not a 6-digit numeric code.</p>
     */
    @JsonAlias({"zipcode", "areaCode", "areaName"})
    @NotBlank(message = "Area name is required")
    @Size(max = 50, message = "Area name must not exceed 50 characters")
    private String areaName;

    // Latitude and longitude — stored to oulet_location GEOGRAPHY(POINT, 4326)
    private String latitude;
    private String longitude;

    // ── outlet_days table ─────────────────────────────────────────────────────
    @Valid
    private List<FmOutletDayDTO> operatingDays;

    // ── tracking ──────────────────────────────────────────────────────────────
    @Size(max = 100)
    private String uploadedBy;
}
