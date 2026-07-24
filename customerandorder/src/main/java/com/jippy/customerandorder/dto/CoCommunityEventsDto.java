package com.jippy.customerandorder.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Data
public class CoCommunityEventsDto {

    private Integer communityEventId;

    @NotNull(message = "Community Id is not null")
    private Integer communityId;

    @NotNull(message = "Event Title is not null")
    private String eventTitle;

    private String eventDescription;

    private String imageUrl;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "Delivery Time Date is not null")
    private LocalDateTime deliveryTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "Event Start Date is not null")
    private LocalDateTime eventStartDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "Event End Date is not null")
    private LocalDateTime eventEndDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "Booking StartDate is not null")
    private LocalDateTime bookingStartDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "Booking End Date is not null")
    private LocalDateTime bookingEndDate;

    @NotNull(message = "Location Name is not null")
    private String locationName;

    private LocalDateTime createdAt;

    private  Integer createdBy;

    @NotNull(message =  "Outlet Id is not null")
    private Integer outletId;

    @NotNull(message = "Maximum members are required")
    private Integer maxMembers;

}
