package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.service.IFmAutoApprovalSchedulerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller used only for testing the Auto Approval Scheduler.
 *
 * Note:
 * This API can be removed once the scheduler is verified.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fm/auto-approval")
@Tag(name = "Auto Approval Scheduler",
        description = "APIs for testing Auto Approval Scheduler")
public class FmAutoApprovalSchedulerController {

    /**
     * Auto Approval Scheduler Service
     */
    private final IFmAutoApprovalSchedulerService autoApprovalSchedulerService;

    /**
     * Executes Auto Approval Scheduler manually.
     *
     * This endpoint is only for testing.
     *
     * @return Success Message
     */
    @PostMapping("/autoApprovalManualTestProcess")
    @Operation(summary = "Execute Auto Approval Scheduler" ,
            description = "This endpoint is only for testing , Actual EndPoint is Run's Through Scheduler " +
                    "which Auto approves for OUTLET, MERCHANT and DRIVER.")
    public ResponseEntity<String> processAutoApprovalRequests() {

        autoApprovalSchedulerService.processAutoApprovalRequests();

        return ResponseEntity.ok("Auto Approval Scheduler Executed Successfully.");
    }

}