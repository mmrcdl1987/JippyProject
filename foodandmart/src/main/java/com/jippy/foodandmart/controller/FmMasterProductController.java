package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.*;
import com.jippy.foodandmart.entity.FmMasterProduct;
import com.jippy.foodandmart.serviceImpl.FmMasterProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * REST controller for the master product catalogue.
 * Base path: /api/master-products
 */
@Slf4j
@RestController
@RequestMapping("/api/fm/master-products")
@RequiredArgsConstructor
public class FmMasterProductController {

    private final FmMasterProductService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FmMasterProduct create(@RequestBody FmMasterProductRequest req) {
        log.info("[MASTER] POST /api/master-products — name={}", req.getMasterProductName());
        return service.save(req);
    }

    @PostMapping("/bulk-add")
    @ResponseStatus(HttpStatus.CREATED)
    public List<FmMasterProduct> bulkAdd(@RequestBody List<FmMasterProductRequest> requests) {
        log.info("[MASTER] POST /api/master-products/bulk-add — count={}", requests.size());
        return service.saveAll(requests);
    }

    @GetMapping
    public List<FmMasterProduct> getAll() {
        log.info("[MASTER] GET /api/master-products");
        return service.getAll();
    }

    @GetMapping("/{id}")
    public FmMasterProduct getById(@PathVariable Integer id) {
        return service.getById(id);
    }

    @GetMapping("/filter")
    public List<FmMasterProduct> filter(@RequestParam String type) {
        return service.filter(type);
    }

    @GetMapping("/search")
    public List<FmMasterProduct> search(@RequestParam String keyword) {
        return service.search(keyword);
    }

    @PutMapping("/{id}")
    public FmMasterProduct update(@PathVariable Integer id, @RequestBody FmMasterProductRequest req) {
        log.info("[MASTER] PUT /api/master-products/{}", id);
        return service.update(id, req);
    }

    @PostMapping("/{id}/photo")
    public ResponseEntity<Map<String, String>> uploadPhoto(
            @PathVariable Integer id,
            @RequestParam("photo") MultipartFile photo) {
        log.info("[MASTER] POST /api/master-products/{}/photo — file={}", id, photo.getOriginalFilename());
        String uri = service.updatePhoto(id, photo);
        return ResponseEntity.ok(Map.of("photo", uri));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        log.info("[MASTER] DELETE /api/master-products/{}", id);
        service.delete(id);
    }

    @PostMapping("/compare-file")
    public ResponseEntity<FmCompareFileResponse> compareFile(
            @RequestParam("file") MultipartFile file) {
        log.info("[MASTER] POST /api/master-products/compare-file — file={}", file.getOriginalFilename());
        return ResponseEntity.ok(service.compareFileWithDB(file));
    }

    /**
     * POST /api/master-products/add-new-items
     * Bulk-saves NEW (non-duplicate) products from compare result into master_products.
     * Silently skips any names that already exist.
     */
    @PostMapping("/add-new-items")
    @ResponseStatus(HttpStatus.CREATED)
    public List<FmMasterProduct> addNewItems(@RequestBody List<FmMasterProductRequest> requests) {
        log.info("[MASTER] POST /api/master-products/add-new-items — count={}", requests.size());
        return service.saveAll(requests);
    }

    @Operation(
            summary = "Create Master Product",
            description = """
                Creates a new master product under an existing category.

                Business Flow:
                • Merchant selects an existing category.
                • If the required product is unavailable, merchant creates a new master product.
                • The API validates the category.
                • Prevents duplicate product names within the same category.
                • Inserts the new product into jippy_fm.master_products.
                • The product becomes available in GET /api/fm/{categoryId} after refresh.
                """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Master product created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "409", description = "Master product already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/create")
    public ResponseEntity<FmApiResponse<FmCreateMasterProductResponseDto>> createMasterProduct(
            @Valid @RequestBody FmCreateMasterProductRequestDto request) {

        log.info("CREATE_MASTER_PRODUCT_API_STARTED | categoryId={} | productName={}",
                request.getCategoryId(),
                request.getMasterProductName());

        FmCreateMasterProductResponseDto response =
                service.createMasterProduct(request);

        log.info("CREATE_MASTER_PRODUCT_API_COMPLETED | masterProductId={}",
                response.getMasterProductId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(FmApiResponse.success(
                        "Master Product created successfully",
                        response));
    }

    @Operation(
            summary = "Get Master Products By Category",
            description = """
                Returns all published master products for the specified category.

                Business Flow:
                • Merchant selects a category.
                • System retrieves all published products belonging to that category.
                • Optional keyword performs case-insensitive product name search.
                • Used while merchant adds products to an outlet.
                """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<FmApiResponse<List<FmMasterProductResponseDto>>> getProductsByCategory(
            @Parameter(
                    description = "Category Id",
                    required = true,
                    example = "5")
            @PathVariable Integer categoryId,

            @Parameter(
                    description = "Optional product name search keyword",
                    example = "Chicken")
            @RequestParam(required = false) String keyword) {

        log.info("GET_PRODUCTS_BY_CATEGORY_STARTED | categoryId={} | keyword={}",
                categoryId, keyword);

        List<FmMasterProductResponseDto> products =
                service.getProductsByCategory(categoryId, keyword);

        return ResponseEntity.ok(
                FmApiResponse.success(
                        "Products fetched successfully",
                        products));
    }
}
