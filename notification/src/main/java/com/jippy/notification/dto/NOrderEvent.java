package com.jippy.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NOrderEvent {

    private String orderId;

    private Integer customerId;

    private Integer outletId;

    private Integer driverId;

    private String status;

    /*
     * SPECIALIZED OUTLET FLOW
     */
    private Integer areaId;

    private Integer rejectedOutletId;
}