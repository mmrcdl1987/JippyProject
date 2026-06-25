package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.FmApiResponse;
import com.jippy.foodandmart.dto.FmCreateCategoryRequestDto;
import com.jippy.foodandmart.dto.FmCreateCategoryResponseDto;
import com.jippy.foodandmart.entity.FmCategory;
import com.jippy.foodandmart.repository.FmCategoryRepository;
import com.jippy.foodandmart.service.IFmCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequestMapping("/api/fm")
@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final IFmCategoryService categoryService;
    private final FmCategoryRepository categoryRepository;


    /**
     * Creates a new global category.
     *
     * This API inserts only into:
     *  - jippy_fm.categories
     *
     * Newly created categories will be visible through
     * GET /api/fm/categories after refresh.
     */
    @PostMapping("/categories")
    public ResponseEntity<FmApiResponse<FmCreateCategoryResponseDto>> createCategory(
            @Valid @RequestBody FmCreateCategoryRequestDto request) {

        log.info("CREATE_CATEGORY_API_STARTED | categoryName={}",
                request.getCategoryName());

        FmCreateCategoryResponseDto response =
                categoryService.createCategory(request);

        log.info("CREATE_CATEGORY_API_COMPLETED | categoryId={}",
                response.getCategoryId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        FmApiResponse.success(
                                "Category created successfully",
                                response
                        )
                );
    }

    @GetMapping("/categories")
    public ResponseEntity<FmApiResponse<List<FmCategory>>> getAllCategories() {
        log.info("[CATEGORY] GET /api/categories");
        List<FmCategory> cats = categoryRepository.findAll();
        return ResponseEntity.ok(FmApiResponse.success("Categories fetched", cats));
    }
}