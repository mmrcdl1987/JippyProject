package com.jippy.foodandmart.controller;
import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.dto.FmCustomerNearbyResponseDto;
import com.jippy.foodandmart.dto.*;
import com.jippy.foodandmart.entity.FmOutlet;
import com.jippy.foodandmart.exception.InvalidUserTypeException;
import com.jippy.foodandmart.exception.ResourceNotFoundException;
import com.jippy.foodandmart.repository.FmMerchantRepository;
import com.jippy.foodandmart.service.FmSpecializedOutletService;
import com.jippy.foodandmart.service.IFmOutletService;
import com.jippy.foodandmart.service.S3Service;
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

import java.io.InputStream;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;

/**
 * REST controller for all outlet management endpoints.
 *
 * <p>Handles single outlet creation, queries, and bulk upload via
 * CSV or Excel files. File parsing (Excel/CSV → DTO list) lives here
 * in private helpers to keep the service layer free of file-format concerns.</p>
 */
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


    /**
     * Creates a single outlet from a JSON request body.
     *
     * <p>POST /api/outlets</p>
     *
     * <p>Why {@code @Valid}: the DTO carries JSR-303 annotations (NotBlank,
     * Pattern for phone, etc.). Spring validates them before the method body
     * runs, returning a 400 with field-level errors if any fail.</p>
     *
     * @param dto the validated outlet creation request
     * @return 201 with an {@link FmOutletCreatedDTO} including portal credentials
     */
    @PostMapping(value = "/bulkUpload", consumes = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<FmApiResponse<FmOutletCreatedDTO>> createOutlet(
    public ResponseEntity<FmApiResponse<FmOutletCreatedDTO>> createOutletForBulkUploadAndOtpValidation(@RequestHeader("Create-Outlet-Token") String token, @Valid @RequestBody FmOutletRequestDTO dto) {

        log.info("[OUTLET] POST /api/outlets name={}, merchantId={}, phone={}", dto.getOutletName(), dto.getMerchantId(), dto.getOutletPhone());
//        FmOutletCreatedDTO saved = outletService.createOutlet(dto);
        FmOutletCreatedDTO saved = outletService.createOutletForBulkUploadAndOtpValidation(dto);

        log.info("[OUTLET] Created: outletId={}", saved.getOutletId());
        return ResponseEntity.status(HttpStatus.CREATED).body(FmApiResponse.success("Outlet created successfully", saved));
    }

    //    --------------- CREATE OUTLET WITH ADDRESS AND BANK DETAILS -----------------------------------------
    @PostMapping(value = "/createOutlet", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Outlet created successfully"), @ApiResponse(responseCode = "400", description = "Validation Failed"), @ApiResponse(responseCode = "404", description = "Merchant Not Found"), @ApiResponse(responseCode = "409", description = "Duplicate Resource")})
    public ResponseEntity<FmApiResponse<FmOutletCreateResponseDTO>> createOutlet(@Valid @RequestBody FmOutletRequestDTO dto) {

        log.info("Received request to create outlet: {}", dto.getOutletName());

        FmOutletCreateResponseDTO response = outletService.createOutlet(dto);

        log.info("Outlet created successfully. outletId={}", response.getOutletId());

        return ResponseEntity.status(HttpStatus.CREATED).body(FmApiResponse.success("Outlet created successfully", response));
    }

    @PostMapping(value = "/{outletId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Outlet image uploaded/updated successfully"), @ApiResponse(responseCode = "400", description = "Invalid image"), @ApiResponse(responseCode = "404", description = "Outlet Not Found"), @ApiResponse(responseCode = "413", description = "Image size exceeds 5 MB")})
    public ResponseEntity<FmApiResponse<String>> uploadOrUpdateOutletImage(

            @PathVariable("outletId") Integer outletId,

            @RequestPart("image") MultipartFile image) {

        log.info("Received outlet image upload/update request. outletId={}, fileName={}", outletId, image != null ? image.getOriginalFilename() : null);

        String imageUrl = outletService.uploadOrUpdateOutletImage(outletId, image);

        log.info("Outlet image uploaded/updated successfully. outletId={}", outletId);

        return ResponseEntity.ok(FmApiResponse.success("Outlet image uploaded successfully", imageUrl));
    }


    @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Outlet image uploaded successfully"), @ApiResponse(responseCode = "400", description = "Invalid image"), @ApiResponse(responseCode = "404", description = "Merchant Not Found"), @ApiResponse(responseCode = "413", description = "Image size exceeds 5 MB")})
    public ResponseEntity<FmApiResponse<String>> uploadOutletImage(

            @RequestParam("merchantId") Integer merchantId,

            @RequestPart("image") MultipartFile image) {

        log.info("Received outlet image upload request. merchantId={}, fileName={}", merchantId, image != null ? image.getOriginalFilename() : null);

        if (!merchantRepository.existsById(merchantId)) {
            throw new ResourceNotFoundException("Merchant not found with id: " + merchantId);
        }

        String imageUrl = s3Service.uploadOutletImage(image, merchantId);

        log.info("Outlet image uploaded successfully. merchantId={}", merchantId);

        return ResponseEntity.ok(FmApiResponse.success("Outlet image uploaded successfully", imageUrl));
    }
    //---------------------------------------------------------------------------------

    /**
     * Updates outlet details by Merchant.
     * <p>
     * This API allows a merchant to update
     * outlet information including:
     * - Outlet Details
     * - Bank Details
     * - Address Details
     * - Operating Days
     * <p>
     * Username and Password cannot be updated
     * through this API.
     */
    @PutMapping("/updateOutletDetailsByMerchant/{outletId}")
    @Operation(summary = "Update Outlet Details By Merchant ", description = "Allows Merchant to update outlet details," + " address, bank details and operating days. " + "[Username and Password cannot be updated].")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Outlet updated successfully"), @ApiResponse(responseCode = "400", description = "Invalid request"), @ApiResponse(responseCode = "404", description = "Outlet or Merchant not found")})
    public ResponseEntity<FmApiResponse<FmUpdateOutletRequestDTO>> updateOutletDetailsByMerchant(@PathVariable Integer outletId, @Valid @RequestBody FmUpdateOutletRequestDTO dto) {

        log.info("Received request to update outlet details for outletId : {}", outletId);

        FmUpdateOutletRequestDTO response = outletService.updateOutletDetailsByMerchant(outletId, dto);

        log.info("Outlet details updated successfully for outletId : {}", outletId);

        return ResponseEntity.ok(FmApiResponse.success("Outlet details updated successfully", response));
    }

    //    --------------------------------------------------------------------------------------------
    //edit and Update outlet product details
    @PutMapping("/editAndUpdateOutletProducts")
    @Operation(summary = "Update outlet details", description = "Updates outlet timings, categories, products and product timings. " + "OutletId, outletName and outletPhone are not editable.")
    public ResponseEntity<FmOutletDetailsDto> updateOutletDetailsByMerchant(

            @Parameter(description = "Outlet ID", required = true) @RequestParam Integer outletId, @RequestParam String userType, @RequestBody FmOutletDetailsDto dto) {

        log.info("Received request to update outlet with id={}", outletId);

        // Call service
        FmOutletDetailsDto response = outletService.updateOutletDetails(outletId, dto, userType);

        log.info("Successfully updated outlet with id={}", outletId);

        return ResponseEntity.ok(response);
    }

    //    -----------------------------------------------------------------------------------------
//    for getOutletDetails API - to fetch outlet details including menu, categories,
//    product timings and outlet timings based on user type (customer or merchant)
    @Operation(summary = "Get Outlet Details", description = """
            Fetches complete outlet details including outlet information, address,
            bank details, operating days, categories and products. based on user type
            
            Prerequisites - to use this API:
            1. Merchant should be created.
            2. Outlet should be created.
            3. Outlet Category should be created and mapped to the outlet.
            4. At least one Product should be created under the Outlet Category.
            5. (Optional) Product Online Pricing can be configured.
            6. (Optional) Product Available Timings can be configured.
            -- Ignore if  already Created/Exists.
            
            Note:
            - If the outlet has no Outlet Categories or Products, this API will not return any data.
            - For CUSTOMER userType, favourite status is returned when customerId is provided.
            - For MERCHANT userType, complete outlet configuration details are returned.
            """)
    @ApiResponse(responseCode = "200", description = "Outlet details fetched successfully")
    @ApiResponse(responseCode = "400", description = "Invalid userType")
    @ApiResponse(responseCode = "404", description = "Outlet not found")
    @GetMapping("/getOutletDetails")
    public ResponseEntity<FmOutletDetailsDto> getOutletDetails(

            @Parameter(description = "Outlet ID", required = true) @RequestParam Integer outletId,

            @Parameter(description = "User Type (CUSTOMER / MERCHANT)", required = true) @RequestParam String userType,

            @Parameter(description = "Customer ID (Optional). If provided, the " + "response includes the is_favourite feild status of the outlet " + "for that customer.", required = false) @RequestParam(required = false) Integer customerId) {

        log.info("Fetching outlet details for outletId={}, userType={}, customerId={}", outletId, userType, customerId);
        // Custom validation for case sensitivity
        if (!FmAppConstants.TYPE_CUSTOMER.equalsIgnoreCase(userType) && !FmAppConstants.TYPE_MERCHANT.equalsIgnoreCase(userType)) {
            throw new InvalidUserTypeException("Invalid userType. Allowed values: CUSTOMER or MERCHANT");
        }

        FmOutletDetailsDto outletDetails = outletService.getOutletDetails(outletId, userType, customerId);

        log.info("Successfully fetched outlet details for outletId: {}, userType: {}", outletId, userType);

        return ResponseEntity.ok(outletDetails);
    }

    @Operation(summary = "Get Outlets by Merchant ID", description = "Fetch all outlets for a " + "merchant with state, city, and area details. Throws error if outlet is not approved.")
    //    for getOutletsByMerchant API - to fetch outlet's, address-state,city,area details based on merchant id
    @ApiResponse(responseCode = "200", description = "Outlets fetched successfully")
    @ApiResponse(responseCode = "400", description = "Outlet not approved")
    @ApiResponse(responseCode = "404", description = "No outlets found")
    @GetMapping("/getOutletsByMerchant")
    public ResponseEntity<List<FmOutletByMerchantDto>> getOutletsByFmMerchant(

            @Parameter(description = "Merchant ID", required = true) @Positive(message = "Merchant ID must be a positive number") @RequestParam Integer merchantId) {

        log.info("Fetching outlets for merchantId={}", merchantId);

        List<FmOutletByMerchantDto> OutletByMerchantDetails = outletService.getOutletsByFmMerchantId(merchantId);

        log.info("Successfully fetched outlets for merchantId={}", merchantId);

        return ResponseEntity.ok(OutletByMerchantDetails);
    }

//    -----------------------------------------------------------------------------------------

    /**
     * Returns all outlets as summary DTOs.
     *
     * <p>GET /api/outlets</p>
     *
     * @return 200 with list of {@link FmOutletSummaryDTO}
     */
    @GetMapping
    public ResponseEntity<FmApiResponse<List<FmOutletSummaryDTO>>> getAllOutlets() {
        log.info("[OUTLET] GET /api/outlets");
        return ResponseEntity.ok(FmApiResponse.success("Outlets fetched", outletService.getAllOutletsSummary()));
    }

    /**
     * Returns all outlets belonging to the specified merchant.
     *
     * <p>GET /api/outlets/merchant/{merchantId}</p>
     *
     * @param merchantId the merchant's primary key
     * @return 200 with list of {@link FmOutletSummaryDTO} for that merchant
     */
    @GetMapping("/merchant/{merchantId}")
    public ResponseEntity<FmApiResponse<List<FmOutletSummaryDTO>>> getOutletsByMerchant(@PathVariable Integer merchantId) {
        log.info("[OUTLET] GET /api/outlets/merchant/{}", merchantId);
        return ResponseEntity.ok(FmApiResponse.success("Outlets fetched", outletService.getOutletsByMerchantId(merchantId)));
    }

    /**
     * Fetches a single outlet by its primary key.
     *
     * <p>GET /api/outlets/{id}</p>
     *
     * @param outletId the outlet's primary key
     * @return 200 with the {@link FmOutlet} entity
     */
//    @GetMapping("/{id}")
//    public ResponseEntity<FmApiResponse<FmOutlet>> getOutletById(@PathVariable Integer id) {
//        log.info("[OUTLET] GET /api/outlets/{}", id);
//        return ResponseEntity.ok
//                (FmApiResponse.success("Outlet fetched", outletService.getOutletById(id)));
//    }
    @GetMapping("/getOutletById/{outletId}")
    @Operation(summary = "Get outlet details by outlet ID", description = "Fetches complete outlet details including outlet information, KYC, bank details, address, location and operating days")
    @ApiResponse(responseCode = "200", description = "Outlet details fetched successfully")
    @ApiResponse(responseCode = "404", description = "Outlet not found")
    public ResponseEntity<FmApiResponse<FmOutletResponseDto>> getOutletById(@PathVariable Integer outletId) {

        log.info("[OUTLET] GET /api/fm/outlets/getOutletById/{}", outletId);

        FmOutletResponseDto response = outletService.getOutletById(outletId);

        log.info("Successfully returned outlet details for outletId={}", outletId);

        return ResponseEntity.ok(FmApiResponse.success("Outlet fetched successfully", response));
    }

    /**
     * Returns the total outlet count.
     *
     * <p>GET /api/outlets/count</p>
     *
     * @return 200 with the count as a Long
     */
    @GetMapping("/count")
    public ResponseEntity<FmApiResponse<Long>> getCount() {
        return ResponseEntity.ok(FmApiResponse.success("Count fetched", outletService.countOutlets()));
    }

    /**
     * Accepts a bulk outlet upload file (.xlsx or .csv) and creates one outlet per data row.
     *
     * <p>POST /api/outlets/upload</p>
     *
     * <p>Why parse in the controller: the service layer only receives a clean
     * {@code List<OutletRequestDTO>}. Parsing is a presentation-layer concern
     * (it deals with file format, column headers, and cell types) and belongs
     * here rather than in the service.</p>
     *
     * <p>The XLS template has a header row (row 0) and an indicator/instruction
     * row (row 1). Data rows start at index 2. The CSV template skips the
     * indicator row unless its first column is "req".</p>
     *
     * @param file the uploaded .xlsx or .csv file
     * @return 200/207/400 depending on success/partial success/full failure
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FmApiResponse<FmBulkOutletResultDTO>> uploadFile(@RequestParam("file") MultipartFile file) {

        log.info("[BULK] POST /api/outlets/upload file={}, size={} bytes", file.getOriginalFilename(), file.getSize());

        if (file.isEmpty())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(FmApiResponse.error("Uploaded file is empty"));

        List<FmOutletRequestDTO> rows;
        try {
            String fn = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
            if (fn.endsWith(".xlsx") || fn.endsWith(".xls")) {
                rows = parseExcel(file.getInputStream());
            } else if (fn.endsWith(".csv")) {
                rows = parseCsv(file.getInputStream());
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(FmApiResponse.error("Only .xlsx or .csv files are supported"));
            }
        } catch (Exception e) {
            log.error("[BULK] File parse error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(FmApiResponse.error("Failed to parse file: " + e.getMessage()));
        }

        if (rows.isEmpty())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(FmApiResponse.error("No data rows found in file"));

        //FmBulkOutletResultDTO result = outletService.bulkUpload(rows);
        FmBulkOutletResultDTO result = outletBulkUpload(rows);
        String message = String.format("Upload complete: %d success, %d failed out of %d rows", result.getSuccessCount(), result.getFailureCount(), result.getTotalRows());
        HttpStatus status = result.getFailureCount() == 0 ? HttpStatus.OK : (result.getSuccessCount() == 0 ? HttpStatus.BAD_REQUEST : HttpStatus.MULTI_STATUS);
        return ResponseEntity.status(status).body(FmApiResponse.success(message, result));
    }

    // ─── File Parsers ─────────────────────────────────────────────────────────

    /**
     * Parses an Excel (.xlsx) InputStream into a list of outlet request DTOs.
     *
     * <p>Why skip row index 1: the Jippy outlet upload template has a header
     * on row 0 and an instruction/indicator row on row 1 (showing column
     * requirements). Data rows start at index 2.</p>
     *
     * <p>Why use {@link XSSFWorkbook}: we only support .xlsx (OOXML format).
     * Old .xls files would need {@code HSSFWorkbook}; if needed, use
     * {@code WorkbookFactory.create(is)} which detects both automatically.</p>
     *
     * @param is the raw Excel file input stream
     * @return list of parsed {@link FmOutletRequestDTO} (empty rows skipped)
     * @throws Exception if the stream cannot be parsed as a valid Excel file
     */
    private List<FmOutletRequestDTO> parseExcel(InputStream is) throws Exception {
        List<FmOutletRequestDTO> list = new ArrayList<>();
        try (Workbook wb = new XSSFWorkbook(is)) {
            Sheet sheet = wb.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) return list;
            // Build a case-insensitive, no-whitespace column index map for flexible header matching
            Map<String, Integer> colMap = new HashMap<>();
            for (int i = 0; i <= headerRow.getLastCellNum(); i++) {
                Cell c = headerRow.getCell(i);
                if (c != null) colMap.put(c.toString().trim().toLowerCase().replaceAll("\\s+", ""), i);
            }
            // Row 1 is the template instruction row — skip it; data starts at row index 2
            for (int r = 2; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                if (getCellStr(row, colMap, "outletname").isBlank()) continue; // skip empty rows
                list.add(mapExcelRow(row, colMap));
            }
        }
        return list;
    }

    /**
     * Parses a CSV InputStream into a list of outlet request DTOs.
     *
     * <p>Why check for "req" in the second row: the CSV template mirrors the
     * Excel template which has an indicator row. If the first column of the
     * second row is "req", it is the indicator row and should be skipped.</p>
     *
     * @param is the raw CSV file input stream
     * @return list of parsed {@link FmOutletRequestDTO}
     * @throws Exception if the stream cannot be read
     */
    private List<FmOutletRequestDTO> parseCsv(InputStream is) throws Exception {
        List<FmOutletRequestDTO> list = new ArrayList<>();
        try (Scanner sc = new Scanner(is)) {
            if (!sc.hasNextLine()) return list;
            String[] headers = sc.nextLine().split(",", -1);
            Map<String, Integer> colMap = new HashMap<>();
            for (int i = 0; i < headers.length; i++)
                colMap.put(headers[i].trim().toLowerCase().replaceAll("\\s+", ""), i);
            if (sc.hasNextLine()) {
                String peek = sc.nextLine();
                String first = peek.split(",")[0].trim().toLowerCase();
                // Skip the indicator row if present (first cell is "req")
                if (!first.equals("req") && !first.isBlank()) list.add(mapCsvRow(peek.split(",", -1), colMap));
            }
            while (sc.hasNextLine()) {
                String[] cells = sc.nextLine().split(",", -1);
                if (cells.length == 0 || cells[0].isBlank()) continue;
                list.add(mapCsvRow(cells, colMap));
            }
        }
        return list;
    }

    /**
     * Maps a single Excel row to an {@link FmOutletRequestDTO}.
     *
     * <p>Why "zipcode" for area code: the XLS column header uses "zipcode"
     * (a legacy label). Internally the value is now an area name string resolved
     * to area_id by the service; the column header stays "zipcode" so existing files work.</p>
     *
     * <p>Why "state" column contains a name (not an ID): Excel templates are
     * filled by humans. The service layer resolves the state name to an integer
     * FK via the states lookup table.</p>
     *
     * @param row the Excel row to map
     * @param col the column-name-to-index map built from the header row
     * @return a populated {@link FmOutletRequestDTO}
     */
    private FmOutletRequestDTO mapExcelRow(Row row, Map<String, Integer> col) {
        FmOutletRequestDTO dto = new FmOutletRequestDTO();
        dto.setOutletName(getCellStr(row, col, "outletname"));
        dto.setMerchantId(parseIntOrNull(getCellStr(row, col, "merchantid")));
        dto.setCuisineType(
                Arrays.stream(getCellStr(row, col, "outletcuisine").split(","))
                        .map(String::trim)
                        .filter(value -> !value.isEmpty())
                        .map(Integer::valueOf)
                        .toArray(Integer[]::new)
        );
        dto.setOutletPhone(getCellStr(row, col, "outletphone"));
        dto.setBuildingNumber(getCellStr(row, col, "buildingnumber"));
        dto.setRoad(getCellStr(row, col, "road"));
        dto.setLandmark(getCellStr(row, col, "arealandmark"));
        dto.setCityId(parseIntOrNull(getCellStr(row, col, "city")));
        // "state" column holds the state name — resolved to state_id in the service layer
        dto.setStateName(getCellStr(row, col, "state"));
        // "zipcode" column now holds an area name string (e.g. "Banjara Hills")
        // The service resolves this to area_id via the area table
        dto.setAreaName(getCellStr(row, col, "zipcode"));
        dto.setLatitude(getCellStr(row, col, "latitude"));
        dto.setLongitude(getCellStr(row, col, "longitude"));
        dto.setUploadedBy("bulk_upload");
        // Build operating days from the per-day columns in the sheet
        dto.setOperatingDays(buildOperatingDays(k -> getCellStr(row, col, k)));
        return dto;
    }

    /**
     * Maps a single CSV row (array of cell strings) to an {@link FmOutletRequestDTO}.
     *
     * <p>Why identical structure to {@link #mapExcelRow}: the CSV and Excel
     * templates share the same column schema. Only the cell-value extraction
     * method differs ({@code csvGet} vs {@code getCellStr}).</p>
     *
     * @param cells the split CSV cell array for this row
     * @param col   the column-name-to-index map built from the header row
     * @return a populated {@link FmOutletRequestDTO}
     */
    private FmOutletRequestDTO mapCsvRow(String[] cells, Map<String, Integer> col) {
        FmOutletRequestDTO dto = new FmOutletRequestDTO();
        dto.setOutletName(csvGet(cells, col, "outletname"));
        dto.setMerchantId(parseIntOrNull(csvGet(cells, col, "merchantid")));
        String cuisineType = csvGet(cells, col, "outletcuisine");

        dto.setCuisineType(
                cuisineType == null || cuisineType.isBlank()
                        ? null
                        : Arrays.stream(cuisineType.split(","))
                        .map(String::trim)
                        .filter(value -> !value.isEmpty())
                        .map(Integer::valueOf)
                        .toArray(Integer[]::new)
        );        dto.setOutletPhone(csvGet(cells, col, "outletphone"));
        dto.setBuildingNumber(csvGet(cells, col, "buildingnumber"));
        dto.setRoad(csvGet(cells, col, "road"));
        dto.setLandmark(csvGet(cells, col, "arealandmark"));
        dto.setCityId(parseIntOrNull(csvGet(cells, col, "city")));
        dto.setStateName(csvGet(cells, col, "state"));
        // "zipcode" column now holds an area name string — resolved to area_id in service
        dto.setAreaName(csvGet(cells, col, "zipcode"));
        dto.setLatitude(csvGet(cells, col, "latitude"));
        dto.setLongitude(csvGet(cells, col, "longitude"));
        dto.setUploadedBy("bulk_upload");
        dto.setOperatingDays(buildOperatingDays(k -> csvGet(cells, col, k)));
        return dto;
    }

    /**
     * Builds a list of {@link FmOutletDayDTO} from day column values in the upload file.
     *
     * <p>Why a {@link Function} parameter: both Excel and CSV rows need this
     * logic but get their cell values differently. Accepting a lambda that maps
     * a column key to a string value avoids duplicating the day-parsing logic.</p>
     *
     * <p>Cell value format: blank/empty = closed, "no"/"closed" = closed,
     * "09:00-22:00" = open with those hours, any other non-blank = open with
     * default hours (09:00–22:00).</p>
     *
     * @param getter a function that returns a cell value for a given column key
     * @return list of 7 {@link FmOutletDayDTO} (Mon–Sun), one per day
     */
    private List<FmOutletDayDTO> buildOperatingDays(Function<String, String> getter) {
        String[] dayKeys = {"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"};
        List<FmOutletDayDTO> days = new ArrayList<>();
        for (int i = 0; i < dayKeys.length; i++) {
            String val = getter.apply(dayKeys[i]);
            boolean isOpen = !val.equalsIgnoreCase("no") && !val.equalsIgnoreCase("closed") && !val.isBlank();
            String open = "09:00", close = "22:00";
            // Parse "HH:mm-HH:mm" range from cell if present
            if (val.contains("-")) {
                String[] parts = val.split("-");
                if (parts.length == 2) {
                    open = parts[0].trim();
                    close = parts[1].trim();
                }
            }
            // Build OutletDayDTO using setter methods instead of builder
            FmOutletDayDTO dayDto = new FmOutletDayDTO();
            dayDto.setDayOfWeekId(i + 1);
            dayDto.setIsOpen(isOpen);
            dayDto.setOpeningTime(LocalTime.parse(open));
            dayDto.setClosingTime(LocalTime.parse(close));
            days.add(dayDto);
        }
        return days;
    }

    // ─── Cell Value Helpers ───────────────────────────────────────────────────

    /**
     * Extracts the string value from an Excel cell at the given column key.
     *
     * <p>Why a column-key lookup helper: cell index must be looked up from the
     * header map every time. This helper keeps row-mapping methods readable
     * by hiding that plumbing.</p>
     *
     * @param row the Excel row
     * @param col the column-name-to-index map
     * @param key the normalised column name to look up
     * @return the cell's string value, or empty string if column not found
     */
    private String getCellStr(Row row, Map<String, Integer> col, String key) {
        Integer idx = col.get(key);
        if (idx == null) return "";
        return getCellStr(row.getCell(idx));
    }

    /**
     * Converts an Apache POI {@link Cell} to a plain string.
     *
     * <p>Why handle NUMERIC specially: numeric cells that represent integers
     * (e.g. merchant ID 42) are stored as doubles (42.0) in Excel. Without
     * this conversion, the string would be "42.0" and {@code Integer.parseInt}
     * would fail.</p>
     *
     * @param c the cell to convert (may be null)
     * @return string representation, or empty string if null
     */
    private String getCellStr(Cell c) {
        if (c == null) return "";
        return switch (c.getCellType()) {
            case NUMERIC -> {
                double d = c.getNumericCellValue();
                // Return integer representation for whole numbers (e.g. 42.0 → "42")
                yield d == Math.floor(d) ? String.valueOf((long) d) : String.valueOf(d);
            }
            case BOOLEAN -> String.valueOf(c.getBooleanCellValue());
            default -> c.toString().trim();
        };
    }

    /**
     * Extracts a cell value from a CSV row array by column name.
     *
     * <p>Why bounds-check {@code idx >= cells.length}: a short row (fewer
     * columns than the header) would throw an ArrayIndexOutOfBoundsException
     * without this guard. We return empty string to treat missing cells as blank.</p>
     *
     * @param cells the split CSV cell array
     * @param col   the column-name-to-index map
     * @param key   the column name to look up
     * @return the trimmed cell string, or empty string if out of bounds
     */
    private String csvGet(String[] cells, Map<String, Integer> col, String key) {
        Integer idx = col.get(key);
        if (idx == null || idx >= cells.length) return "";
        return cells[idx].trim();
    }

    /**
     * Parses a string to an Integer, returning null if blank or unparseable.
     *
     * <p>Why strip ".0": Excel numeric cells serialise as "42.0". Stripping
     * the trailing ".0" before parsing prevents {@code NumberFormatException}.</p>
     *
     * @param s the raw cell string
     * @return the parsed integer, or null if not parseable
     */
    private Integer parseIntOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return Integer.parseInt(s.trim().replaceAll("\\.0$", ""));
        } catch (Exception e) {
            return null;
        }
    }


    public FmBulkOutletResultDTO outletBulkUpload(List<FmOutletRequestDTO> rows) {
        int total = rows.size(), success = 0;
        List<FmBulkOutletResultDTO.OutletCredential> credentials = new ArrayList<>();
        List<FmBulkOutletResultDTO.OutletError> errors = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            int rowNum = i + 3;
            FmOutletRequestDTO dto = rows.get(i);
            try {
                FmOutletCreatedDTO created = outletService.createOutletBulkUpload(dto);
                success++;
                FmBulkOutletResultDTO.OutletCredential cred = new FmBulkOutletResultDTO.OutletCredential();
                cred.setOutletId(created.getOutletId());
                cred.setOutletName(created.getOutletName());
                credentials.add(cred);
            } catch (Exception e) {
                log.warn("[BULK] Row {} failed: {}", rowNum, e.getMessage());
                FmBulkOutletResultDTO.OutletError err = new FmBulkOutletResultDTO.OutletError();
                err.setRowNumber(rowNum);
                err.setOutletName(dto.getOutletName());
                err.setReason(e.getMessage());
                errors.add(err);
                //throw new RuntimeException(e.getMessage());
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

    @Operation(summary = "Customer App: Nearby outlets within 3 km (USE THIS FOR MOBILE APP)", description = """
            Returns all active outlets (is_active = 'Y') within 3 km of the customer's
            current GPS location, sorted nearest-first.
            
            This is the correct endpoint for the mobile/customer app. It returns:
              • distanceKm      — straight-line distance via PostGIS
              • roadDistance    — actual road distance via Google Maps (e.g. "1.4 km")
              • deliveryTime    — estimated delivery time via Google Maps (e.g. "14 mins")
              • openingTime     — today's opening time from outlet_days table
              • closingTime     — today's closing time from outlet_days table
              • openNow         — whether the outlet is currently open
            
            Prerequisites for non-null roadDistance and deliveryTime:
              1. Set google.maps.api-key in application.yml (or GOOGLE_MAPS_API_KEY env var)
              2. Enable Distance Matrix API in Google Cloud Console
            
            Prerequisites for non-null distanceKm:
              1. outlet_location must be set in jippy_fm.outlets for each outlet
              2. Run: UPDATE jippy_fm.outlets
                        SET outlet_location = ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)
                      WHERE outlet_location IS NULL;
            
            Example:
              GET /api/outlets/customer/nearby?lat=17.385&lng=78.4867
            """)
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Nearby outlets fetched successfully", content = @Content(schema = @Schema(implementation = FmCustomerNearbyResponseDto.class))), @ApiResponse(responseCode = "400", description = "lat or lng parameter is missing / invalid")})
    @GetMapping("/customer/nearby")
    public ResponseEntity<FmCustomerNearbyResponseDto> fetchCustomerNearbyOutlets(@Parameter(description = "Customer latitude (GPS)", example = "17.385", required = true) @RequestParam double lat,

                                                                                  @Parameter(description = "Customer longitude (GPS)", example = "78.4867", required = true) @RequestParam double lng, @RequestParam(required = false) Integer categoryId) {

        log.info("GET /api/outlets/customer/nearby lat={}, lng={}", lat, lng);
        FmCustomerNearbyResponseDto response = outletService.fetchCustomerNearbyOutlets(lat, lng, categoryId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Public Customer Nearby Outlets", description = """
            Public version of the nearby outlets API.

            Returns only the minimal outlet fields:
            outletId, outletName, merchantId, review, isActive, isApproved,
            distanceKm, isVegOutlet, outletPicUrl.

            This endpoint does not require authentication.
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Nearby outlets fetched successfully"),
            @ApiResponse(responseCode = "400", description = "lat or lng parameter is missing / invalid")
    })
    @GetMapping("/public/customer/nearby")
    public ResponseEntity<FmPublicCustomerNearbyResponseDto> fetchPublicCustomerNearbyOutlets(
            @Parameter(description = "Customer latitude (GPS)", example = "17.385", required = true)
            @RequestParam double lat,
            @Parameter(description = "Customer longitude (GPS)", example = "78.4867", required = true)
            @RequestParam double lng) {

        log.info("GET /api/fm/outlets/public/customer/nearby lat={}, lng={}", lat, lng);
        FmPublicCustomerNearbyResponseDto response = outletService.fetchPublicCustomerNearbyOutlets(lat, lng);
        return ResponseEntity.ok(response);
    }

    // End Points for getting address data from FM_Microservice to CO_Microservice
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

    //    for Feign  in the CO_Microservice to just fetch
    @GetMapping("/fetchOutletName")
    public ResponseEntity<String> fetchOutletName(@RequestParam @Positive(message = "Outlet ID must be a positive number") Integer outletId) {

        return ResponseEntity.ok(outletService.fetchOutletName(outletId));
    }


    @GetMapping("/location/{outletId}")
    public ResponseEntity<OutletLocationResponseDto> getOutletLocation(@PathVariable Integer outletId) {
        log.info("REST request to get location for outletId: {}", outletId);

        OutletLocationResponseDto response = outletService.getOutletLocation(outletId);


        log.debug("Returning location response for outletId: {}", outletId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/specialized-outlets/area")
    public ResponseEntity<FmNearbyOutletResponseDto> fetchSpecializedOutletsByAreaId(@RequestParam Integer areaId) {

        log.info("Fetching specialized outlets for areaId={}", areaId);

        FmNearbyOutletResponseDto response = service.fetchSpecializedOutletsByAreaId(areaId);

        log.info("Successfully fetched {} outlets for areaId={}", response.getTotalOutlets(), areaId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/specialized-outlets/nearby")
    public ResponseEntity<FmNearbyOutletResponseDto> fetchNearbySpecializedOutlets(@RequestParam Double latitude, @RequestParam Double longitude) {

        log.info("Fetching nearby specialized outlets for latitude={} longitude={}", latitude, longitude);

        return ResponseEntity.ok(service.fetchNearbySpecializedOutlets(latitude, longitude));
    }

    // used as a feign client call
    @GetMapping("/getOutletAddressDetails")
    public ResponseEntity<OutletLocationResponseDto> getOutletAddressDetails(@RequestParam Integer outletId) {
        log.info("REST request to get location for outletId: {}", outletId);

        OutletLocationResponseDto response = outletService.getOutletAddressDetails(outletId);


        log.debug("Returning location response for outletId: {}", outletId);

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
