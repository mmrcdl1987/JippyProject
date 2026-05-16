package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.FmCompareFileResponse;
import com.jippy.foodandmart.dto.FmMasterProductRequest;
import com.jippy.foodandmart.entity.FmMasterProduct;
import com.jippy.foodandmart.serviceImpl.FmMasterProductService;
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
}
