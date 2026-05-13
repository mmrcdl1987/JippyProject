package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.FmApiResponse;
import com.jippy.foodandmart.dto.FmBulkUploadResultDTO;
import com.jippy.foodandmart.dto.FmMerchantWithBankDto;
import com.jippy.foodandmart.dto.FmMerchantRequestDTO;
import com.jippy.foodandmart.entity.FmMerchant;
import com.jippy.foodandmart.service.IFmMerchantService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/fm/merchants")
@RequiredArgsConstructor
@Slf4j
public class FmMerchantController {

    private final IFmMerchantService merchantService;

    /** POST /api/merchants — create a single merchant */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FmApiResponse<FmMerchant>> createMerchant(
            @Valid @RequestBody FmMerchantRequestDTO dto) {

        log.info("[MERCHANT] POST /api/merchants email={}, phone={}", dto.getEmail(), dto.getPhone());
        FmMerchant saved = merchantService.createMerchant(dto);
        log.info("[MERCHANT] Created: merchantId={}", saved.getMerchantId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(FmApiResponse.success("Merchant registered successfully", saved));
    }

    /** GET /api/merchants — list all */
    @GetMapping
    public ResponseEntity<FmApiResponse<List<FmMerchant>>> getAllMerchants() {
        log.info("[MERCHANT] GET /api/merchants");
        return ResponseEntity.ok(FmApiResponse.success("Merchants fetched", merchantService.getAllMerchants()));
    }

    /** GET /api/merchants/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<FmApiResponse<FmMerchant>> getMerchantById(@PathVariable Integer id) {
        log.info("[MERCHANT] GET /api/merchants/{}", id);
        return ResponseEntity.ok(FmApiResponse.success("Merchant fetched", merchantService.getMerchantById(id)));
    }

    /** GET /api/merchants/count */
    @GetMapping("/count")
    public ResponseEntity<FmApiResponse<Long>> getCount() {
        return ResponseEntity.ok(FmApiResponse.success("Count fetched", merchantService.countMerchants()));
    }

    /** POST /api/merchants/upload — bulk upload via .xlsx or .csv */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FmApiResponse<FmBulkUploadResultDTO>> uploadFile(
            @RequestParam("file") MultipartFile file) {

        log.info("[BULK] POST /api/merchants/upload file={}, size={} bytes",
                file.getOriginalFilename(), file.getSize());

        if (file.isEmpty())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(FmApiResponse.error("Uploaded file is empty"));

        FmBulkUploadResultDTO result = merchantService.bulkUpload(file);
        String message = String.format("Upload complete: %d success, %d failed out of %d rows",
                result.getSuccessCount(), result.getFailureCount(), result.getTotalRows());

        HttpStatus status = result.getFailureCount() == 0 ? HttpStatus.OK
                : (result.getSuccessCount() == 0 ? HttpStatus.BAD_REQUEST : HttpStatus.MULTI_STATUS);

        return ResponseEntity.status(status).body(FmApiResponse.success(message, result));
    }

    // update merchant with bank details
    @Operation(
            summary = "Update Merchant Profile",
            description = "Update merchant and bank details"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Merchant updated successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Merchant or bank not found")
    @PutMapping("/updateMerchantProfile")
    public ResponseEntity<FmMerchantWithBankDto> updateMerchantProfile(@Valid
                                                                       @RequestBody FmMerchantWithBankDto dto) {
        log.info("Updating merchant profile with data: {}", dto);

        FmMerchantWithBankDto updated =
                merchantService.updateMerchantProfile(dto);
        log.info("Successfully updated merchant profile for merchantId: {}", dto.getMerchantId());

        return ResponseEntity.ok(updated);
    }
    //    get merchant details with bank details
    @Operation(
            summary = "Get Merchant Profile with Bank Details",
            description = "Fetch merchant details along with bank information using merchant ID"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Merchant fetched successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Merchant not found")
    @GetMapping("/getMerchantProfile")
    public ResponseEntity<FmMerchantWithBankDto> getMerchantProfile(
            @RequestParam Long merchantId) {
        log.info("Fetching merchant profile for merchantId: {}", merchantId);
        FmMerchantWithBankDto response =
                merchantService.getMerchantWithBank(merchantId);
        log.info("Successfully fetched merchant profile for merchantId: {}", merchantId);
        return ResponseEntity.ok(response);
    }
}
