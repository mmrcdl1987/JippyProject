package com.jippy.foodandmart.service;

//import com.jippy.foodandmart.dto.FmApprovalRequestDTO;

import org.springframework.stereotype.Service;

/**
 * Service interface for Approval Requests.
 *
 * Responsible for creating Approval Requests
 * whenever a new Entity enters the Approval Workflow.
 */
@Service
public interface IFmApprovalRequestService {

    /**
     * Creates a new Approval Request.
     * <p>
     * Business Rules:
     * 1. Entity Type can be MERCHANT, OUTLET or DRIVER.
     * 2. Current Level is initialized as Level 1.
     * 3. Status is initialized as PENDING.
     *
     * @param entityType Entity Type (MERCHANT / OUTLET / DRIVER)
     * @param entityId   Merchant Id / Outlet Id / Driver Id
     * @param createdBy  Logged-in User Id
     */
    void createApprovalRequest(String entityType, Integer entityId, Integer createdBy);

//    void createApprovalRequest(FmApprovalRequestDTO requestDTO);
}