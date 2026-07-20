package com.jippy.driver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO used to create Approval Requests
 * in Food & Mart Microservice.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverApprovalRequestDTO {

    /** Entity Type.
     * Example: DRIVER*/
    private String entityType;

//    Driver Id
    private Integer entityId;

//    Created By
    private Integer createdBy;

}