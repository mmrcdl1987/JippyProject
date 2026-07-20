package com.jippy.foodandmart.service;

//import com.jippy.foodandmart.dto.FmApprovalRequestDTO;

import com.jippy.foodandmart.dto.FmApprovalRequestDTO;
import com.jippy.foodandmart.dto.FmLevel1PendingApprovalResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;

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

//    For Driver Microservice Through Feign Client
    void createApprovalRequest(FmApprovalRequestDTO requestDTO);

    List<FmLevel1PendingApprovalResponseDTO> getLevel1PendingApprovalRequests(Integer approverId);
}