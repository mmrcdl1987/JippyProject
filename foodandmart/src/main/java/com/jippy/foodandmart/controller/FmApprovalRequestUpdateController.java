package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.FmApprovalRequestUpdateRequestDTO;
import com.jippy.foodandmart.dto.FmApprovalRequestUpdateResponseDTO;
import com.jippy.foodandmart.service.IFmApprovalRequestUpdateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for Approval Request Management.
 * <p>
 * This API is used by an approver to Approve or Reject
 * one or more Approval Requests.
 */
@RestController
@RequestMapping("/api/fm/approval-requests")
@RequiredArgsConstructor
@Slf4j
public class FmApprovalRequestUpdateController {

    private final IFmApprovalRequestUpdateService approvalRequestUpdateService;

    /**
     * Approves or Rejects one or more Approval Requests.
     * <p>
     * If Approved
     * -> Approval Transaction will be created.
     * -> Approval Level will be updated.
     * -> Final Entity will be approved after last level.
     * <p>
     * If Rejected
     * -> Approval Transaction will be created.
     * -> Approval Request Status becomes REJECTED.
     */
    @Operation(summary = "Approve or Reject Approval Requests",
            description = "Updates one or more Approval Requests by changing their approval status. "
                    + "Creates Approval Transaction history, updates Approval Workflow " +
                    "and approves the respective Entity after the final approval level.")
            @ApiResponse(responseCode = "200", description = "Approval Requests Updated Successfully")
            @ApiResponse(responseCode = "400", description = "Validation Failed")
            @ApiResponse(responseCode = "404", description = "Approval Request Not Found")
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    @PostMapping("/updateApprovalRequestsToApproved")
    public ResponseEntity<FmApprovalRequestUpdateResponseDTO> updateApprovalRequestsToApproved(

            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody
                    (description = "Approval Request Update Details", required = true,
                            content = @Content(examples = @ExampleObject(value = """
                    {
                      "approvalRequestIds":[2,3,4],
                      "status":"APPROVED",
                      "rejectedReason":null,
                      "approverId":14
                    }
                    """))) @RequestBody FmApprovalRequestUpdateRequestDTO requestDTO) {

        log.info("Received request to update Approval Requests : {}", requestDTO);

        FmApprovalRequestUpdateResponseDTO responseDTO = approvalRequestUpdateService.updateApprovalRequestsToApproved(requestDTO);

        log.info("Approval Requests Updated Successfully.");

        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

}