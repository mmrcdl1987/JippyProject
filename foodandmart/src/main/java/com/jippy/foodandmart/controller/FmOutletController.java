package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.dto.*;
import com.jippy.foodandmart.repository.FmMerchantRepository;
import com.jippy.foodandmart.service.FmSpecializedOutletService;
import com.jippy.foodandmart.service.IFmOutletService;
import com.jippy.foodandmart.service.S3Service;
import com.jippy.foodandmart.exception.InvalidUserTypeException;
import com.jippy.foodandmart.exception.ResourceNotFoundException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;

@RestController
@Validated
@RequestMapping("/api/fm/outlets")
@RequiredArgsConstructor
@Slf4j
public class FmOutletController {

    private final IFmOutletService outletService;
    private final FmSpecializedOutletService service;
    private final S3Service s3Service;
    private final FmMerchantRepository merchantRepository;


    // ============================================================
    // CREATE OUTLET - BULK / OTP VALIDATION FLOW
    // ============================================================

    @PostMapping(value = "/bulkUpload", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FmApiResponse<FmOutletCreatedDTO>> createOutletForBulkUploadAndOtpValidation(@RequestHeader("Create-Outlet-Token") String token, @Valid @RequestBody FmOutletRequestDTO dto) {

        log.info("[OUTLET] POST /api/outlets/bulkUpload name={}, merchantId={}, phone={}", dto.getOutletName(), dto.getMerchantId(), dto.getOutletPhone());

        FmOutletCreatedDTO saved = outletService.createOutletForBulkUploadAndOtpValidation(dto);

        log.info("[OUTLET] Created: outletId={}", saved.getOutletId());

        return ResponseEntity.status(HttpStatus.CREATED).body(FmApiResponse.success("Outlet created successfully", saved));
    }


    // ============================================================
    // CREATE OUTLET
    // ============================================================

    @PostMapping(value = "/createOutlet", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Outlet created successfully"), @ApiResponse(responseCode = "400", description = "Validation Failed"), @ApiResponse(responseCode = "404", description = "Merchant Not Found"), @ApiResponse(responseCode = "409", description = "Duplicate Resource")})
    public ResponseEntity<FmApiResponse<FmOutletCreateResponseDTO>> createOutlet(@Valid @RequestBody FmOutletRequestDTO dto) {

        log.info("Received request to create outlet: {}", dto.getOutletName());

        FmOutletCreateResponseDTO response = outletService.createOutlet(dto);

        log.info("Outlet created successfully. outletId={}", response.getOutletId());

        return ResponseEntity.status(HttpStatus.CREATED).body(FmApiResponse.success("Outlet created successfully", response));
    }


    // ============================================================
    // OUTLET IMAGE
    // ============================================================

    @PostMapping(value = "/{outletId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Outlet image uploaded/updated successfully"), @ApiResponse(responseCode = "400", description = "Invalid image"), @ApiResponse(responseCode = "404", description = "Outlet Not Found"), @ApiResponse(responseCode = "413", description = "Image size exceeds 5 MB")})
    public ResponseEntity<FmApiResponse<String>> uploadOrUpdateOutletImage(@PathVariable("outletId") Integer outletId, @RequestPart("image") MultipartFile image) {

        log.info("Received outlet image upload/update request. outletId={}, fileName={}", outletId, image != null ? image.getOriginalFilename() : null);

        String imageUrl = outletService.uploadOrUpdateOutletImage(outletId, image);

        return ResponseEntity.ok(FmApiResponse.success("Outlet image uploaded successfully", imageUrl));
    }


    @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Outlet image uploaded successfully"), @ApiResponse(responseCode = "400", description = "Invalid image"), @ApiResponse(responseCode = "404", description = "Merchant Not Found"), @ApiResponse(responseCode = "413", description = "Image size exceeds 5 MB")})
    public ResponseEntity<FmApiResponse<String>> uploadOutletImage(@RequestParam("merchantId") Integer merchantId, @RequestPart("image") MultipartFile image) {

        log.info("Received outlet image upload request. merchantId={}, fileName={}", merchantId, image != null ? image.getOriginalFilename() : null);

        if (!merchantRepository.existsById(merchantId)) {
            throw new ResourceNotFoundException("Merchant not found with id: " + merchantId);
        }

        String imageUrl = s3Service.uploadOutletImage(image, merchantId);

        return ResponseEntity.ok(FmApiResponse.success("Outlet image uploaded successfully", imageUrl));
    }


    // ============================================================
    // UPDATE OUTLET
    // ============================================================

    @PutMapping("/updateOutletDetailsByMerchant/{outletId}")
    @Operation(summary = "Update Outlet Details By Merchant", description = "Allows Merchant to update outlet details, address, " + "bank details and operating days. " + "[Username and Password cannot be updated].")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Outlet updated successfully"), @ApiResponse(responseCode = "400", description = "Invalid request"), @ApiResponse(responseCode = "404", description = "Outlet or Merchant not found")})
    public ResponseEntity<FmApiResponse<FmUpdateOutletRequestDTO>> updateOutletDetailsByMerchant(@PathVariable Integer outletId, @Valid @RequestBody FmUpdateOutletRequestDTO dto) {

        FmUpdateOutletRequestDTO response = outletService.updateOutletDetailsByMerchant(outletId, dto);

        return ResponseEntity.ok(FmApiResponse.success("Outlet details updated successfully", response));
    }


    // ============================================================
    // EDIT OUTLET PRODUCT DETAILS
    // ============================================================

    @PutMapping("/editAndUpdateOutletProducts")
    @Operation(summary = "Update outlet details", description = "Updates outlet timings, categories, products and product timings. " + "OutletId, outletName and outletPhone are not editable.")
    public ResponseEntity<FmOutletDetailsDto> updateOutletDetailsByMerchant(@Parameter(description = "Outlet ID", required = true) @RequestParam Integer outletId,

                                                                            @RequestParam String userType,

                                                                            @RequestBody FmOutletDetailsDto dto) {

        FmOutletDetailsDto response = outletService.updateOutletDetails(outletId, dto, userType);

        return ResponseEntity.ok(response);
    }


    // ============================================================
    // GET OUTLET DETAILS
    // ============================================================

    @Operation(summary = "Get Outlet Details", description = """
            Fetches complete outlet details including outlet information,
            address, bank details, operating days, categories and products.
            based on user type.
            """)
    @ApiResponse(responseCode = "200", description = "Outlet details fetched successfully")
    @ApiResponse(responseCode = "400", description = "Invalid userType")
    @ApiResponse(responseCode = "404", description = "Outlet not found")
    @GetMapping("/getOutletDetails")
    public ResponseEntity<FmOutletDetailsDto> getOutletDetails(@RequestParam Integer outletId, @RequestParam String userType, @RequestParam(required = false) Integer customerId) {

        if (!FmAppConstants.TYPE_CUSTOMER.equalsIgnoreCase(userType) && !FmAppConstants.TYPE_MERCHANT.equalsIgnoreCase(userType)) {

            throw new InvalidUserTypeException("Invalid userType. Allowed values: CUSTOMER or MERCHANT");
        }

        FmOutletDetailsDto outletDetails = outletService.getOutletDetails(outletId, userType, customerId);

        return ResponseEntity.ok(outletDetails);
    }


    // ============================================================
    // GET OUTLETS BY MERCHANT
    // ============================================================

    @GetMapping("/getOutletsByMerchant")
    public ResponseEntity<List<FmOutletByMerchantDto>> getOutletsByFmMerchant(@Positive(message = "Merchant ID must be a positive number") @RequestParam Integer merchantId) {

        List<FmOutletByMerchantDto> outletByMerchantDetails = outletService.getOutletsByFmMerchantId(merchantId);

        return ResponseEntity.ok(outletByMerchantDetails);
    }


    // ============================================================
    // GET ALL OUTLETS
    // ============================================================

    @GetMapping
    public ResponseEntity<FmApiResponse<List<FmOutletSummaryDTO>>> getAllOutlets() {

        return ResponseEntity.ok(FmApiResponse.success("Outlets fetched", outletService.getAllOutletsSummary()));
    }


    // ============================================================
    // GET OUTLETS BY MERCHANT ID
    // ============================================================

    @GetMapping("/merchant/{merchantId}")
    public ResponseEntity<FmApiResponse<List<FmOutletSummaryDTO>>> getOutletsByMerchant(@PathVariable Integer merchantId) {

        return ResponseEntity.ok(FmApiResponse.success("Outlets fetched", outletService.getOutletsByMerchantId(merchantId)));
    }


    // ============================================================
    // GET OUTLET BY ID
    // ============================================================

    @GetMapping("/getOutletById/{outletId}")
    @Operation(summary = "Get outlet details by outlet ID")
    @ApiResponse(responseCode = "200", description = "Outlet details fetched successfully")
    @ApiResponse(responseCode = "404", description = "Outlet not found")
    public ResponseEntity<FmApiResponse<FmOutletResponseDto>> getOutletById(@PathVariable Integer outletId) {

        FmOutletResponseDto response = outletService.getOutletById(outletId);

        return ResponseEntity.ok(FmApiResponse.success("Outlet fetched successfully", response));
    }


    // ============================================================
    // OUTLET COUNT
    // ============================================================

    @GetMapping("/count")
    public ResponseEntity<FmApiResponse<Long>> getCount() {

        return ResponseEntity.ok(FmApiResponse.success("Count fetched", outletService.countOutlets()));
    }


    // ============================================================
    // BULK UPLOAD
    // ============================================================

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FmApiResponse<FmBulkOutletResultDTO>> uploadFile(@RequestParam("file") MultipartFile file) {

        log.info("[BULK] POST /api/fm/outlets/upload file={}, size={} bytes", file.getOriginalFilename(), file.getSize());

        if (file == null || file.isEmpty()) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(FmApiResponse.error("Uploaded file is empty"));
        }

        List<FmOutletRequestDTO> rows;

        try {

            String fn = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase(Locale.ROOT) : "";

            if (fn.endsWith(".xlsx") || fn.endsWith(".xls")) {

                rows = parseExcel(file.getInputStream());

            } else if (fn.endsWith(".csv")) {

                rows = parseCsv(file.getInputStream());

            } else {

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(FmApiResponse.error("Only .xlsx or .csv files are supported"));
            }

        } catch (Exception e) {

            log.error("[BULK] File parse error: {}", e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(FmApiResponse.error("Failed to parse file: " + e.getMessage()));
        }

        if (rows.isEmpty()) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(FmApiResponse.error("No data rows found in file"));
        }

        FmBulkOutletResultDTO result = outletBulkUpload(rows);

        String message = String.format("Upload complete: %d success, %d failed out of %d rows", result.getSuccessCount(), result.getFailureCount(), result.getTotalRows());

        HttpStatus status = result.getFailureCount() == 0 ? HttpStatus.OK : (result.getSuccessCount() == 0 ? HttpStatus.BAD_REQUEST : HttpStatus.MULTI_STATUS);

        return ResponseEntity.status(status).body(FmApiResponse.success(message, result));
    }


    // ============================================================
    // EXCEL PARSER
    // ============================================================

    private List<FmOutletRequestDTO> parseExcel(InputStream is) throws Exception {

        List<FmOutletRequestDTO> list = new ArrayList<>();

        try (Workbook wb = new XSSFWorkbook(is)) {

            if (wb.getNumberOfSheets() == 0) {
                return list;
            }

            Sheet sheet = wb.getSheetAt(0);

            Row headerRow = sheet.getRow(0);

            if (headerRow == null) {
                return list;
            }

            Map<String, Integer> colMap = new HashMap<>();

            for (int i = 0; i < headerRow.getLastCellNum(); i++) {

                Cell cell = headerRow.getCell(i);

                if (cell == null) {
                    continue;
                }

                String header = normalizeHeader(cell.toString());

                if (header.isBlank()) {
                    continue;
                }

                colMap.put(header, i);
            }

            validateRequiredColumns(colMap, "Excel");

            for (int r = 2; r <= sheet.getLastRowNum(); r++) {

                Row row = sheet.getRow(r);

                if (row == null || isEmptyExcelRow(row)) {

                    continue;
                }

                String outletName = getCellStr(row, colMap, "outletname");

                if (outletName == null || outletName.isBlank()) {
                    continue;
                }

                FmOutletRequestDTO dto = mapExcelRow(row, colMap);

                list.add(dto);
            }
        }

        log.info("[BULK] Excel parsing completed. rows={}", list.size());

        return list;
    }


    // ============================================================
    // EXCEL EMPTY ROW
    // ============================================================

    private boolean isEmptyExcelRow(Row row) {

        if (row == null) {
            return true;
        }

        short lastCellNum = row.getLastCellNum();

        if (lastCellNum < 0) {
            return true;
        }

        for (int i = 0; i < lastCellNum; i++) {

            Cell cell = row.getCell(i);

            if (cell == null) {
                continue;
            }

            String value = cell.toString().trim();

            if (!value.isBlank()) {
                return false;
            }
        }

        return true;
    }


    // ============================================================
    // CSV PARSER
    // ============================================================

    private List<FmOutletRequestDTO> parseCsv(InputStream is) throws Exception {

        List<FmOutletRequestDTO> list = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String headerLine = reader.readLine();

            if (headerLine == null || headerLine.isBlank()) {
                return list;
            }

            String[] headers = splitCsvLine(headerLine);

            Map<String, Integer> colMap = new HashMap<>();

            for (int i = 0; i < headers.length; i++) {

                String header = normalizeHeader(headers[i]);

                if (header.isBlank()) {
                    continue;
                }

                colMap.put(header, i);
            }

            validateRequiredColumns(colMap, "CSV");

            String line;

            int csvRowNumber = 1;

            while ((line = reader.readLine()) != null) {

                csvRowNumber++;

                if (line.isBlank()) {
                    continue;
                }

                String[] cells = splitCsvLine(line);

                if (cells.length == 0) {
                    continue;
                }

                if (isIndicatorCsvRow(cells)) {

                    log.info("[BULK] CSV row {} detected as instruction row. Skipping.", csvRowNumber);

                    continue;
                }

                String outletName = getCsvCell(cells, colMap, "outletname");

                if (outletName == null || outletName.isBlank()) {
                    continue;
                }

                FmOutletRequestDTO dto = mapCsvRow(cells, colMap);

                list.add(dto);
            }
        }

        log.info("[BULK] CSV parsing completed. rows={}", list.size());

        return list;
    }


    // ============================================================
    // REQUIRED COLUMNS
    // ============================================================

    private void validateRequiredColumns(Map<String, Integer> colMap, String fileType) {

        String[] requiredColumns = {

                "outletname", "merchantname", "cuisinetype", "outletphone", "outletemail",

                "isvegoutlet", "isgstapplied",

                "fssainumber", "gstnumber", "username", "password",

                "accountnumber", "ifsccode", "bankname", "accountholdername",

                "buildingnumber", "road", "landmark",

                "statename", "cityname", "areaname",

                "latitude", "longitude",

                "operatingdays",

                "uploadedby"};

        for (String requiredColumn : requiredColumns) {

            if (!colMap.containsKey(requiredColumn)) {

                throw new IllegalArgumentException("Required " + fileType + " column is missing: " + requiredColumn);
            }
        }
    }


    // ============================================================
    // CSV INDICATOR ROW
    // ============================================================

    private boolean isIndicatorCsvRow(String[] cells) {

        if (cells == null || cells.length == 0) {
            return false;
        }

        int checked = 0;

        int matched = 0;

        for (String cell : cells) {

            if (cell == null) {
                continue;
            }

            String value = cell.trim().toLowerCase(Locale.ROOT);

            if (value.isBlank()) {
                continue;
            }

            checked++;

            if (value.equals("req") || value.equals("required") || value.equals("yes") || value.equals("no") || value.equals("y") || value.equals("n")) {
                matched++;
            }
        }

        return checked > 0 && matched == checked;
    }


    // ============================================================
    // EXCEL ROW MAPPER
    // ============================================================

    private FmOutletRequestDTO mapExcelRow(Row row, Map<String, Integer> col) {

        FmOutletRequestDTO dto = new FmOutletRequestDTO();

        dto.setOutletName(getCellStr(row, col, "outletname"));

        dto.setMerchantName(getCellStr(row, col, "merchantname"));

        dto.setCuisineTypeNames(getCellStr(row, col, "cuisinetype"));

        dto.setOutletPhone(getCellStr(row, col, "outletphone"));

        dto.setOutletEmail(getCellStr(row, col, "outletemail"));

        dto.setAlternateOutletPhone(getCellStr(row, col, "alternateoutletphone"));

        // ========================================================
        // VEG / GST
        // ========================================================

        dto.setIsVegOutlet(parseBooleanValue(getCellStr(row, col, "isvegoutlet"), "is_veg_outlet"));

        dto.setIsGstApplied(parseBooleanValue(getCellStr(row, col, "isgstapplied"), "is_gst_applied"));

        // ========================================================
        // KYC
        // ========================================================

        dto.setFssaiNumber(getCellStr(row, col, "fssainumber"));

        dto.setGstNumber(getCellStr(row, col, "gstnumber"));

        // ========================================================
        // LOGIN
        // ========================================================

        String username = getCellStr(row, col, "username");

        String password = getCellStr(row, col, "password");

        dto.setUsername(username.isBlank() ? null : username);

        dto.setPassword(password.isBlank() ? null : password);

        // ========================================================
        // BANK
        // ========================================================

        dto.setAccountNumber(getCellStr(row, col, "accountnumber"));

        dto.setIfscCode(getCellStr(row, col, "ifsccode"));

        dto.setBankName(getCellStr(row, col, "bankname"));

        dto.setAccountHolderName(getCellStr(row, col, "accountholdername"));

        // ========================================================
        // ADDRESS
        // ========================================================

        dto.setBuildingNumber(getCellStr(row, col, "buildingnumber"));

        dto.setRoad(getCellStr(row, col, "road"));

        dto.setLandmark(getCellStr(row, col, "landmark"));

        // ========================================================
        // LOCATION
        // ========================================================

        dto.setStateName(getCellStr(row, col, "statename"));

        dto.setCityName(getCellStr(row, col, "cityname"));

        dto.setAreaName(getCellStr(row, col, "areaname"));

        dto.setLatitude(getCellStr(row, col, "latitude"));

        dto.setLongitude(getCellStr(row, col, "longitude"));

        // ========================================================
        // MULTIPLE OPERATING TIMES
        // ========================================================

        dto.setOperatingDays(buildOperatingDays(key -> getCellStr(row, col, key)));

        // ========================================================
        // UPLOADED BY
        // ========================================================

        String uploadedBy = getCellStr(row, col, "uploadedby");

        dto.setUploadedBy(uploadedBy.isBlank() ? "bulk_upload" : uploadedBy);

        return dto;
    }


    // ============================================================
    // CSV ROW MAPPER
    // ============================================================

    private FmOutletRequestDTO mapCsvRow(String[] cells, Map<String, Integer> col) {

        FmOutletRequestDTO dto = new FmOutletRequestDTO();

        dto.setOutletName(csvGet(cells, col, "outletname"));

        dto.setMerchantName(csvGet(cells, col, "merchantname"));

        dto.setCuisineTypeNames(csvGet(cells, col, "cuisinetype"));

        dto.setOutletPhone(csvGet(cells, col, "outletphone"));

        dto.setOutletEmail(csvGet(cells, col, "outletemail"));

        dto.setAlternateOutletPhone(csvGet(cells, col, "alternateoutletphone"));

        // ========================================================
        // VEG / GST
        // ========================================================

        dto.setIsVegOutlet(parseBooleanValue(csvGet(cells, col, "isvegoutlet"), "is_veg_outlet"));

        dto.setIsGstApplied(parseBooleanValue(csvGet(cells, col, "isgstapplied"), "is_gst_applied"));

        // ========================================================
        // KYC
        // ========================================================

        dto.setFssaiNumber(csvGet(cells, col, "fssainumber"));

        dto.setGstNumber(csvGet(cells, col, "gstnumber"));

        // ========================================================
        // LOGIN
        // ========================================================

        String username = csvGet(cells, col, "username");

        String password = csvGet(cells, col, "password");

        dto.setUsername(username == null || username.isBlank() ? null : username);

        dto.setPassword(password == null || password.isBlank() ? null : password);

        // ========================================================
        // BANK
        // ========================================================

        dto.setAccountNumber(csvGet(cells, col, "accountnumber"));

        dto.setIfscCode(csvGet(cells, col, "ifsccode"));

        dto.setBankName(csvGet(cells, col, "bankname"));

        dto.setAccountHolderName(csvGet(cells, col, "accountholdername"));

        // ========================================================
        // ADDRESS
        // ========================================================

        dto.setBuildingNumber(csvGet(cells, col, "buildingnumber"));

        dto.setRoad(csvGet(cells, col, "road"));

        dto.setLandmark(csvGet(cells, col, "landmark"));

        // ========================================================
        // LOCATION
        // ========================================================

        dto.setStateName(csvGet(cells, col, "statename"));

        dto.setCityName(csvGet(cells, col, "cityname"));

        dto.setAreaName(csvGet(cells, col, "areaname"));

        dto.setLatitude(csvGet(cells, col, "latitude"));

        dto.setLongitude(csvGet(cells, col, "longitude"));

        // ========================================================
        // MULTIPLE OPERATING TIMES
        // ========================================================

        dto.setOperatingDays(buildOperatingDays(key -> csvGet(cells, col, key)));

        // ========================================================
        // UPLOADED BY
        // ========================================================

        String uploadedBy = csvGet(cells, col, "uploadedby");

        dto.setUploadedBy(uploadedBy == null || uploadedBy.isBlank() ? "bulk_upload" : uploadedBy);

        return dto;
    }


    // ============================================================
    // BOOLEAN PARSER
    // ============================================================

    private Boolean parseBooleanValue(String value, String columnName) {

        if (value == null || value.isBlank()) {
            return false;
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);

        switch (normalized) {

            case "true":
            case "yes":
            case "y":
            case "1":
                return true;

            case "false":
            case "no":
            case "n":
            case "0":
                return false;

            default:
                throw new IllegalArgumentException("Invalid value '" + value + "' for column '" + columnName + "'. Allowed values: true/false, yes/no, y/n, 1/0");
        }
    }


    // ============================================================
    // MULTIPLE OPERATING TIMES
    // ============================================================

    /**
     * Supports multiple timings for the same day.
     * <p>
     * Example:
     * <p>
     * Monday:
     * 09:00-14:00|18:00-22:00
     * <p>
     * Tuesday:
     * 09:00-22:00
     * <p>
     * The "|" character separates multiple slots
     * on the same day.
     */
    private List<FmOutletDayDTO> buildOperatingDays(Function<String, String> getter) {

        String[] dayKeys = {"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"};

        List<FmOutletDayDTO> days = new ArrayList<>();

        for (int i = 0; i < dayKeys.length; i++) {

            String dayValue = getter.apply(dayKeys[i]);

            if (dayValue == null || dayValue.isBlank()) {
                continue;
            }

            dayValue = dayValue.trim();

            /*
             * Example:
             *
             * 09:00-14:00|18:00-22:00
             *
             * becomes:
             *
             * 09:00-14:00
             * 18:00-22:00
             */

            String[] timings = dayValue.split("\\|");

            for (int slotIndex = 0; slotIndex < timings.length; slotIndex++) {

                String timing = timings[slotIndex].trim();

                if (timing.isBlank()) {
                    continue;
                }

                /*
                 * Closed / No
                 */

                if (timing.equalsIgnoreCase("no") || timing.equalsIgnoreCase("closed")) {
                    continue;
                }

                /*
                 * Expected:
                 *
                 * 09:00-14:00
                 */

                String[] parts = timing.split("-", 2);

                if (parts.length != 2) {

                    throw new IllegalArgumentException("Invalid operating time '" + timing + "' for " + dayKeys[i] + ". Expected HH:mm-HH:mm");
                }

                String openingValue = parts[0].trim();

                String closingValue = parts[1].trim();

                try {

                    LocalTime openingTime = LocalTime.parse(openingValue);

                    LocalTime closingTime = LocalTime.parse(closingValue);

                    /*
                     * Validate that opening and closing
                     * are not exactly the same.
                     */

                    if (openingTime.equals(closingTime)) {

                        throw new IllegalArgumentException("Opening time and closing time cannot be the same for " + dayKeys[i]);
                    }

                    FmOutletDayDTO dayDto = new FmOutletDayDTO();

                    /*
                     * Monday = 1
                     * Tuesday = 2
                     * ...
                     * Sunday = 7
                     */

                    dayDto.setDayOfWeekId(i + 1);

                    dayDto.setIsOpen(true);

                    dayDto.setOpeningTime(openingTime);

                    dayDto.setClosingTime(closingTime);

                    /*
                     * Slot type.
                     *
                     * One timing:
                     * FULL_DAY
                     *
                     * First of multiple:
                     * MORNING
                     *
                     * Second:
                     * EVENING
                     *
                     * Third and later:
                     * SLOT_3, SLOT_4...
                     */

                    if (timings.length == 1) {

                        dayDto.setSlotType("FULL_DAY");

                    } else if (slotIndex == 0) {

                        dayDto.setSlotType("MORNING");

                    } else if (slotIndex == 1) {

                        dayDto.setSlotType("EVENING");

                    } else {

                        dayDto.setSlotType("SLOT_" + (slotIndex + 1));
                    }

                    days.add(dayDto);

                } catch (DateTimeException e) {

                    throw new IllegalArgumentException("Invalid operating time '" + timing + "' for " + dayKeys[i] + ". Expected HH:mm-HH:mm");
                }
            }
        }

        return days;
    }


    // ============================================================
    // HEADER NORMALIZATION
    // ============================================================

    private String normalizeHeader(String header) {

        if (header == null) {
            return "";
        }

        String normalized = header
                .replace("\uFEFF", "")
                .trim();

        // Handle headers exported with surrounding CSV quotes.
        // Examples:
        // "outletname"  -> outletname
        // "Outlet Name" -> outletname
        if (normalized.length() >= 2
                && normalized.startsWith("\"")
                && normalized.endsWith("\"")) {

            normalized = normalized.substring(
                    1,
                    normalized.length() - 1
            );
        }

        // Remove BOM again in case it occurs inside quotes.
        normalized = normalized
                .replace("\uFEFF", "")
                .trim();

        // Normalize all supported header formats:
        // Outlet Name -> outletname
        // outlet_name -> outletname
        // outlet-name -> outletname
        // outletname  -> outletname
        return normalized
                .toLowerCase(Locale.ROOT)
                .replaceAll("[\\s_-]+", "");
    }


    // ============================================================
    // CSV GET
    // ============================================================

    private String csvGet(String[] cells, Map<String, Integer> col, String key) {

        Integer index = col.get(normalizeHeader(key));

        if (index == null || index < 0 || index >= cells.length) {
            return "";
        }

        String value = cells[index];

        if (value == null) {
            return "";
        }

        return stripCsvQuotes(value.trim());
    }


    // ============================================================
    // GET CSV CELL
    // ============================================================

    private String getCsvCell(String[] cells, Map<String, Integer> colMap, String columnName) {

        return csvGet(cells, colMap, columnName);
    }


    // ============================================================
    // STRIP CSV QUOTES
    // ============================================================

    private String stripCsvQuotes(String value) {

        if (value == null) {
            return "";
        }

        String result = value.trim();

        if (result.length() >= 2 && result.startsWith("\"") && result.endsWith("\"")) {

            result = result.substring(1, result.length() - 1);

            result = result.replace("\"\"", "\"");
        }

        return result.trim();
    }


    // ============================================================
    // CSV SPLITTER
    // ============================================================

    private String[] splitCsvLine(String line) {

        if (line == null) {
            return new String[0];
        }

        List<String> result = new ArrayList<>();

        StringBuilder current = new StringBuilder();

        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {

            char c = line.charAt(i);

            if (c == '"') {

                if (insideQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {

                    current.append('"');

                    i++;

                } else {

                    insideQuotes = !insideQuotes;
                }

            } else if (c == ',' && !insideQuotes) {

                result.add(current.toString());

                current.setLength(0);

            } else {

                current.append(c);
            }
        }

        result.add(current.toString());

        return result.toArray(new String[0]);
    }


    // ============================================================
    // EXCEL CELL
    // ============================================================

    private String getCellStr(Row row, Map<String, Integer> col, String key) {

        Integer idx = col.get(normalizeHeader(key));

        if (idx == null) {
            return "";
        }

        return getCellStr(row.getCell(idx));
    }


    // ============================================================
    // EXCEL CELL VALUE
    // ============================================================

    private String getCellStr(Cell c) {

        if (c == null) {
            return "";
        }

        return switch (c.getCellType()) {

            case NUMERIC -> {

                double d = c.getNumericCellValue();

                yield d == Math.floor(d) ? String.valueOf((long) d) : String.valueOf(d);
            }

            case BOOLEAN -> String.valueOf(c.getBooleanCellValue());

            case FORMULA -> c.toString().trim();

            default -> c.toString().trim();
        };
    }


    // ============================================================
    // BULK UPLOAD SERVICE
    // ============================================================

    public FmBulkOutletResultDTO outletBulkUpload(List<FmOutletRequestDTO> rows) {

        int total = rows.size();

        int success = 0;

        List<FmBulkOutletResultDTO.OutletCredential> credentials = new ArrayList<>();

        List<FmBulkOutletResultDTO.OutletError> errors = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {

            int rowNum = i + 2;

            FmOutletRequestDTO dto = rows.get(i);

            try {

                log.info("[BULK] Processing row {} outletName={}", rowNum, dto.getOutletName());

                FmOutletCreatedDTO created = outletService.createOutletBulkUpload(dto);

                success++;

                FmBulkOutletResultDTO.OutletCredential cred = new FmBulkOutletResultDTO.OutletCredential();

                cred.setOutletId(created.getOutletId());

                cred.setOutletName(created.getOutletName());

                credentials.add(cred);

                log.info("[BULK] Row {} success. outletId={}", rowNum, created.getOutletId());

            } catch (Exception e) {

                log.warn("[BULK] Row {} failed. outletName={}, reason={}", rowNum, dto.getOutletName(), e.getMessage(), e);

                FmBulkOutletResultDTO.OutletError err = new FmBulkOutletResultDTO.OutletError();

                err.setRowNumber(rowNum);

                err.setOutletName(dto.getOutletName());

                err.setReason(e.getMessage() != null ? e.getMessage() : "Unknown error");

                errors.add(err);
            }
        }

        FmBulkOutletResultDTO result = new FmBulkOutletResultDTO();

        result.setTotalRows(total);

        result.setSuccessCount(success);

        result.setFailureCount(total - success);

        result.setCredentials(credentials);

        result.setErrors(errors);

        return result;
    }


    // ============================================================
    // CUSTOMER NEARBY OUTLETS
    // ============================================================

    @Operation(summary = "Customer App: Nearby outlets within 3 km", description = "Returns all active outlets within 3 km of customer's current GPS location.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Nearby outlets fetched successfully", content = @Content(schema = @Schema(implementation = FmCustomerNearbyResponseDto.class))), @ApiResponse(responseCode = "400", description = "Invalid latitude or longitude")})
    @GetMapping("/customer/nearby")
    public ResponseEntity<FmCustomerNearbyResponseDto> fetchCustomerNearbyOutlets(@RequestParam double lat, @RequestParam double lng, @RequestParam(required = false) Integer categoryId) {

        FmCustomerNearbyResponseDto response = outletService.fetchCustomerNearbyOutlets(lat, lng, categoryId);

        return ResponseEntity.ok(response);
    }


    // ============================================================
    // ADDRESS DETAILS
    // ============================================================

    @PostMapping("/saveAddressDetails")
    public ResponseEntity<FmAddressRequestDto> saveAddressDetails(@Valid @RequestBody FmAddressRequestDto fmAddressRequestDto) {

        FmAddressRequestDto savedAddress = outletService.saveAddressDetails(fmAddressRequestDto);

        return ResponseEntity.ok(savedAddress);
    }


    @GetMapping("/getAddressDetails")
    public ResponseEntity<FmAddressRequestDto> getAddressDetails(@RequestParam Integer driverId) {

        FmAddressRequestDto getAddress = outletService.getAddressDetails(driverId);

        return ResponseEntity.ok(getAddress);
    }


    // ============================================================
    // FETCH OUTLET NAME
    // ============================================================

    @GetMapping("/fetchOutletName")
    public ResponseEntity<String> fetchOutletName(@RequestParam @Positive(message = "Outlet ID must be a positive number") Integer outletId) {

        return ResponseEntity.ok(outletService.fetchOutletName(outletId));
    }


    // ============================================================
    // OUTLET LOCATION
    // ============================================================

    @GetMapping("/location/{outletId}")
    public ResponseEntity<OutletLocationResponseDto> getOutletLocation(@PathVariable Integer outletId) {

        OutletLocationResponseDto response = outletService.getOutletLocation(outletId);

        return ResponseEntity.ok(response);
    }


    // ============================================================
    // SPECIALIZED OUTLETS BY AREA
    // ============================================================

    @GetMapping("/specialized-outlets/area")
    public ResponseEntity<FmNearbyOutletResponseDto> fetchSpecializedOutletsByAreaId(@RequestParam Integer areaId) {

        FmNearbyOutletResponseDto response = service.fetchSpecializedOutletsByAreaId(areaId);

        return ResponseEntity.ok(response);
    }


    // ============================================================
    // SPECIALIZED OUTLETS NEARBY
    // ============================================================

    @GetMapping("/specialized-outlets/nearby")
    public ResponseEntity<FmNearbyOutletResponseDto> fetchNearbySpecializedOutlets(@RequestParam Double latitude, @RequestParam Double longitude) {

        return ResponseEntity.ok(service.fetchNearbySpecializedOutlets(latitude, longitude));
    }


    // ============================================================
    // GET OUTLET ADDRESS DETAILS
    // ============================================================

    @GetMapping("/getOutletAddressDetails")
    public ResponseEntity<OutletLocationResponseDto> getOutletAddressDetails(@RequestParam Integer outletId) {

        OutletLocationResponseDto response = outletService.getOutletAddressDetails(outletId);

        return ResponseEntity.ok(response);
    }

    // ============================================================================
// ADMIN - GET COMPLETE OUTLET DETAILS
// ============================================================================

    @Operation(summary = "Admin: Get Complete Outlet Details", description = """
            Fetches complete outlet configuration based on outlet ID.
            
            The response includes:
            - Outlet details
            - Outlet location
            - Outlet status
            - Bank details
            - Address details
            - Cuisine types
            - Outlet timings
            - Categories
            - Products
            - Product merchant price
            - Product online price
            - Product available timings
            - Product variants
            - Variant merchant price
            - Variant online price
            
            Admin receives both merchant and online pricing.
            """)
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Admin outlet details fetched successfully"), @ApiResponse(responseCode = "400", description = "Invalid outlet ID"), @ApiResponse(responseCode = "404", description = "Outlet not found")})
    @GetMapping("/admin/outlet-details")
    public ResponseEntity<FmAdminOutletDetailsDto> getAdminOutletDetails(

            @Parameter(description = "Outlet ID", required = true, example = "267") @RequestParam Integer outletId) {

        log.info("ADMIN_OUTLET_DETAILS_REQUEST | outletId={}", outletId);

        FmAdminOutletDetailsDto response = outletService.getAdminOutletDetails(outletId);

        log.info("ADMIN_OUTLET_DETAILS_SUCCESS | outletId={}", outletId);

        return ResponseEntity.ok(response);
    }

    // ============================================================================
    // PUBLIC - GET OUTLET DETAILS (NO AUTHENTICATION)
    // ============================================================================

    @Operation(summary = "Public: Get Outlet Details", description = """
            Fetches public outlet configuration based on outlet ID without authentication.

            The response includes:
            - Outlet details (ID, name, availability)
            - Categories with products
            - Product details (name, description, price, veg status, image)
            - Product variants (if available)

            This is a simplified version of the admin endpoint with only essential fields.
            """)
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Public outlet details fetched successfully"), @ApiResponse(responseCode = "400", description = "Invalid outlet ID"), @ApiResponse(responseCode = "404", description = "Outlet not found")})
    @GetMapping("/public/outlet-details")
    public ResponseEntity<FmPublicOutletDetailsDto> getPublicOutletDetails(

            @Parameter(description = "Outlet ID", required = true, example = "267") @RequestParam Integer outletId) {

        log.info("PUBLIC_OUTLET_DETAILS_REQUEST | outletId={}", outletId);

        FmPublicOutletDetailsDto response = outletService.getPublicOutletDetails(outletId);

        log.info("PUBLIC_OUTLET_DETAILS_SUCCESS | outletId={}", outletId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/area/{outletId}")
    public ResponseEntity<Integer> getAreaIdByOutletId(
            @PathVariable Integer outletId) {

        log.info(
                "GET /api/fm/outlets/{}/area",
                outletId
        );

        Integer areaId =
                outletService.getAreaIdByOutletId(outletId);

        return ResponseEntity.ok(areaId);
    }
}
