package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.*;
import com.jippy.foodandmart.service.IFmMenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fm/menu")
@RequiredArgsConstructor
@Slf4j
public class FmMenuController {

    private final IFmMenuService menuService;

    /**
     * GET /api/menu/outlets
     * Returns all active outlets (id, name, cuisine, phone, itemCount)
     * Used by the Copy Menu UI to populate the source/destination pickers.
     */
    @GetMapping("/outlets")
    public ResponseEntity<FmApiResponse<List<FmOutletSummaryDTO>>> listOutlets() {
        log.info("GET /api/menu/outlets");
        List<FmOutletSummaryDTO> outlets = menuService.listAllOutlets();
        return ResponseEntity.ok(FmApiResponse.success("Outlets fetched", outlets));
    }

    /**
     * GET /api/menu/items/{outletId}
     * Returns all menu items for the given outlet.
     */
    @GetMapping("/items/{outletId}")
    public ResponseEntity<FmApiResponse<List<FmMenuItemDTO>>> getMenuItems(
            @PathVariable Integer outletId) {
        log.info("GET /api/menu/items/{}", outletId);
        try {
            List<FmMenuItemDTO> items = menuService.getMenuByOutlet(outletId);
            return ResponseEntity.ok(FmApiResponse.success("Menu items fetched", items));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(FmApiResponse.error(e.getMessage()));
        }
    }

    /**
     * POST /api/menu/copy
     * Copy selected menu items from one outlet to one or more destination outlets.
     *
     * Request body:
     * {
     *   "sourceOutletId": 1,
     *   "destinationOutletIds": [2, 3],
     *   "menuItemIds": [101, 102],      // optional — omit to copy ALL
     *   "options": {
     *     "copyPrices": true,
     *     "copyAvailability": true,
     *     "copyImages": false,
     *     "overwriteExisting": false
     *   }
     * }
     */
    @PostMapping(value = "/copy", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FmApiResponse<FmMenuCopyResultDTO>> copyMenu(
            @RequestBody FmMenuCopyRequestDTO req) {

        log.info("POST /api/menu/copy - sourceOutlet={}, destinations={}, items={}",
                req.getSourceOutletId(),
                req.getDestinationOutletIds(),
                req.getMenuItemIds());

        if (req.getSourceOutletId() == null) {
            return ResponseEntity.badRequest()
                    .body(FmApiResponse.error("sourceOutletId is required"));
        }
        if (req.getDestinationOutletIds() == null || req.getDestinationOutletIds().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(FmApiResponse.error("destinationOutletIds must not be empty"));
        }

        try {
            FmMenuCopyResultDTO result = menuService.copyMenu(req);
            String message = String.format(
                    "Copy complete: %d succeeded, %d failed across %d outlet(s)",
                    result.getSuccessCount(), result.getFailureCount(), result.getTotalOutlets());

            HttpStatus status = result.getFailureCount() == 0 ? HttpStatus.OK
                    : (result.getSuccessCount() == 0 ? HttpStatus.BAD_REQUEST : HttpStatus.MULTI_STATUS);

            return ResponseEntity.status(status)
                    .body(FmApiResponse.success(message, result));

        } catch (IllegalArgumentException e) {
            log.warn("Menu copy failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(FmApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during menu copy", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(FmApiResponse.error("Internal error: " + e.getMessage()));
        }
    }
}
