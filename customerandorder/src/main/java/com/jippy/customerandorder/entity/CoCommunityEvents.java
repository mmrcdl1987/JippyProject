package com.jippy.customerandorder.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Entity
@Data
@Table(name = "community_events", schema = "jippy_customer_and_order")
public class CoCommunityEvents {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer communityEventsId;

    private Integer communityId;

    private String eventTitle;

    private String eventDescription;

    private String imageUrl;

    private LocalDateTime deliveryTime;

    private LocalDateTime eventStartDate;

    private LocalDateTime eventEndDate;

    private LocalDateTime bookingStartDate;

    private LocalDateTime bookingEndDate;

    private String locationName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private  Integer createdBy;

    private Integer updatedBy;

    private Integer outletId;

    private Integer maxMembers;
}
