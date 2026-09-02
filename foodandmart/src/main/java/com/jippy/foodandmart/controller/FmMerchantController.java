package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.*;
import com.jippy.foodandmart.entity.FmMerchant;
import com.jippy.foodandmart.service.IFmMerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(
            summary = "Create Single Merchant",
            description = "Creates a new merchant with merchant details, KYC details, bank details, address details, and merchant login credentials."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Merchant created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid merchant details or validation failed"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Merchant already exists with the provided email, phone, PAN, Aadhaar, FSSAI, or account number"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    @PostMapping(
            value = "/createMerchant",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<FmApiResponse<FmMerchant>> createMerchant(

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Single merchant creation request containing merchant, KYC, bank, and address details. State, city, and area are selected using dropdowns and their IDs are provided.",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "Single Merchant Create Request",
                                    summary = "Create merchant with dropdown-based address IDs",
                                    value = """
                                        {
                                          "firstName": "Rohan",
                                          "lastName": "Vadluri",
                                          "email": "rohan@gmail.com",
                                          "phone": "9876543210",
                                          "username": "rohan123",
                                          "password": "Rohan@123",
                                          "outletType": "Restaurant",
                                          "uploadedBy": "Admin",
                                          "pan": "ABCDE1234F",
                                          "adhar": "987654321012",
                                          "accountNumber": "1234567890123456",
                                          "ifscCode": "SBIN0001234",
                                          "bankLocation": "Kukatpally Branch",
                                          "nameInBankAccount": "Rohan Vadluri",
                                          "dob": "2002-08-15",
                                          "fssai": "12345678901234 (Optional)",
                                          "gstNumber": "36ABCDE1234F1Z5 (Optional)",
                                          "buildingNumber": "12-34 [complete ADDRESS]",
                                          "road": "Main Road - optional",
                                          "landmark": "Near Metro Station - optional",
                                          "stateId": 36,
                                          "cityId": 101,
                                          "areaId": 1001,
                                     
                                        }
                                        """
                            )
                    )
            )
            @Valid @RequestBody FmMerchantRequestDTO dto) {

        log.info(
                "[MERCHANT] Creating merchant: email={}, phone={}",
                dto.getEmail(),
                dto.getPhone()
        );

        FmMerchant merchant = merchantService.createMerchant(dto);

        log.info(
                "[MERCHANT] Merchant created successfully: merchantId={}",
                merchant.getMerchantId()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        FmApiResponse.success(
                                "Merchant created successfully",
                                merchant
                        )
                );
    }
    /** GET /api/merchants — list all */
    @GetMapping
    public ResponseEntity<FmApiResponse<List<FmMerchant>>> getAllMerchants() {
        log.info("[MERCHANT] GET /api/merchants");
        return ResponseEntity.ok(FmApiResponse.success("Merchants fetched", merchantService.getAllMerchants()));
    }

//    ------------------------------------------------------------
    /** GET /api/merchants/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<FmApiResponse<FmMerchantDto>> getMerchantById(@PathVariable Integer id) {
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
    @Operation(summary = "Update Merchant Profile",
            description = "Update merchant and bank details"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Merchant updated successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Merchant or bank not found")
    @PutMapping("/updateMerchantProfile")
    public ResponseEntity<FmMerchantWithBankDto> updateMerchantProfile(
            @Valid @RequestBody FmMerchantWithBankDto dto) {
        log.info("Updating merchant profile with data: {}", dto);

        FmMerchantWithBankDto updated =
                merchantService.updateMerchantProfile(dto);
        log.info("Successfully updated merchant profile for merchantId: {}", dto.getMerchantId());

        return ResponseEntity.ok(updated);
    }

//    --------------------------------------------------------------------------
    //    get merchant details with bank details
    @Operation(summary = "Get Merchant Profile with Bank Details",
            description = "Fetch merchant details along with bank information using merchant ID"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Merchant fetched successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Merchant not found")
    @GetMapping("/getMerchantProfile")
    public ResponseEntity<FmMerchantWithBankDto> getMerchantProfile(
            @RequestParam Integer merchantId) {
        log.info("Fetching merchant profile for merchantId: {}", merchantId);
        FmMerchantWithBankDto response =
                merchantService.getMerchantWithBank(merchantId);
        log.info("Successfully fetched merchant profile for merchantId: {}", merchantId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/fetchByMerchantId")
    public ResponseEntity<FmMerchantDto> fetchByMerchantId(@RequestParam Integer merchantId) {
        log.info("Fetch by MerchantId API called for merchantId: {}", merchantId);
        FmMerchantDto fmMerchantDto = merchantService.getMerchantById(merchantId);
        return ResponseEntity.ok(fmMerchantDto);
    }

    @PutMapping("/updateMerchantProfilePic")
    ResponseEntity<FmResponseDto> updateMerchantProfilePic(@RequestBody  FmMerchantDto merchantDto){

        log.info("Updating merchant profile picture for merchantId: {}", merchantDto.getMerchantId());
        FmResponseDto response = merchantService.updateMerchantProfilePic(merchantDto);
        log.info("Successfully updated merchant profile picture for merchantId: {}", merchantDto.getMerchantId());
        return ResponseEntity.ok(response);
    }
}
