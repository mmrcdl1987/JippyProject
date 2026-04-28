package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.FmApiResponse;
import com.jippy.foodandmart.dto.FmOutletTransferRequestDTO;
import com.jippy.foodandmart.dto.FmOutletTransferResponseDTO;
import com.jippy.foodandmart.service.IFmOutletTransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Outlet Transfer API
 *
 * POST   /api/outlets/transfer
 *          → Transfer an outlet from its current merchant to a new merchant.
 *
 * GET    /api/outlets/{outletId}/transfer-history
 *          → Full audit trail of all past owners of an outlet.
 *
 * GET    /api/merchants/{merchantId}/transfers/inbound
 *          → All outlets a merchant has received.
 *
 * GET    /api/merchants/{merchantId}/transfers/outbound
 *          → All outlets a merchant has given away.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class FmOutletTransferController {

    private final IFmOutletTransferService transferService;

    // ─── Transfer ─────────────────────────────────────────────────────────────

    /**
     * POST /api/outlets/transfer
     * Body: { outletId, toMerchantId, transferReason, transferredBy }
     */
    @PostMapping("/api/outlets/transfer")
    public ResponseEntity<FmApiResponse<FmOutletTransferResponseDTO>> transferOutlet(
            @Valid @RequestBody FmOutletTransferRequestDTO request) {

        log.info("POST /api/outlets/transfer - outletId={}, toMerchantId={}",
                request.getOutletId(), request.getToMerchantId());

        FmOutletTransferResponseDTO result = transferService.transferOutlet(request);

        log.info("Transfer complete: transferId={}", result.getTransferId());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(FmApiResponse.success("Outlet transferred successfully", result));
    }

    // ─── History ──────────────────────────────────────────────────────────────

    /**
     * GET /api/outlets/{outletId}/transfer-history
     * Returns all past ownership changes for the given outlet.
     */
    @GetMapping("/api/outlets/{outletId}/transfer-history")
    public ResponseEntity<FmApiResponse<List<FmOutletTransferResponseDTO>>> getOutletTransferHistory(
            @PathVariable Integer outletId) {

        log.info("GET /api/outlets/{}/transfer-history", outletId);
        List<FmOutletTransferResponseDTO> history = transferService.getHistoryByOutlet(outletId);
        return ResponseEntity.ok(FmApiResponse.success("Transfer history fetched", history));
    }

    /**
     * GET /api/merchants/{merchantId}/transfers/inbound
     * Returns all outlets transferred TO this merchant.
     */
    @GetMapping("/api/merchants/{merchantId}/transfers/inbound")
    public ResponseEntity<FmApiResponse<List<FmOutletTransferResponseDTO>>> getInboundTransfers(
            @PathVariable Integer merchantId) {

        log.info("GET /api/merchants/{}/transfers/inbound", merchantId);
        List<FmOutletTransferResponseDTO> result = transferService.getInboundTransfers(merchantId);
        return ResponseEntity.ok(FmApiResponse.success("Inbound transfers fetched", result));
    }


    /**
     * GET /api/outlets/transfers
     * Returns all transfer records across all outlets (for admin history view).
     */
    @GetMapping("/api/outlets/transfers")
    public ResponseEntity<FmApiResponse<List<FmOutletTransferResponseDTO>>> getAllTransfers() {
        log.info("GET /api/outlets/transfers");
        List<FmOutletTransferResponseDTO> result = transferService.getAllTransfers();
        return ResponseEntity.ok(FmApiResponse.success("All transfers fetched", result));
    }

    /**
     * GET /api/merchants/{merchantId}/transfers/outbound
     * Returns all outlets transferred FROM this merchant.
     */
    @GetMapping("/api/merchants/{merchantId}/transfers/outbound")
    public ResponseEntity<FmApiResponse<List<FmOutletTransferResponseDTO>>> getOutboundTransfers(
            @PathVariable Integer merchantId) {

        log.info("GET /api/merchants/{}/transfers/outbound", merchantId);
        List<FmOutletTransferResponseDTO> result = transferService.getOutboundTransfers(merchantId);
        return ResponseEntity.ok(FmApiResponse.success("Outbound transfers fetched", result));
    }
}
