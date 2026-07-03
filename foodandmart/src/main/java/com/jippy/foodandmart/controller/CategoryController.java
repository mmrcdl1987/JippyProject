package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.FmApiResponse;
import com.jippy.foodandmart.dto.FmCreateCategoryRequestDto;
import com.jippy.foodandmart.dto.FmCreateCategoryResponseDto;
import com.jippy.foodandmart.entity.FmCategory;
import com.jippy.foodandmart.repository.FmCategoryRepository;
import com.jippy.foodandmart.service.IFmCategoryService;
import io.swagger.v3.oas.annotations.Parameter;
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
    @PostMapping("/createCategory")
    public ResponseEntity<FmApiResponse<FmCreateCategoryResponseDto>> createCategory(
            @Valid @RequestBody FmCreateCategoryRequestDto request) {

        log.info("CREATE_CATEGORY_API_STARTED | categoryName={}",
                request.getCategoryName());

        FmCreateCategoryResponseDto response =
                categoryService.createCategory(request);

        log.info("CREATE_CATEGORY_API_COMPLETED | categoryId={}", response.getCategoryId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(FmApiResponse.success("Category created successfully", response));
    }

//    @GetMapping("/Categories")
//    public ResponseEntity<FmApiResponse<List<FmCategory>>> getAllCategories() {
//        log.info("[CATEGORY] GET /api/categories");
//
//        List<FmCategory> cats = categoryRepository.findAll();
//
//        return ResponseEntity.ok(FmApiResponse.success("Categories fetched", cats));
//    }


//    this API is used to fetch categories based on the filter provided.
//    The filter can be either "ALL" or "HOME". If the filter is "ALL", it will fetch all categories.
//    If the filter is "HOME", it will fetch only the categories that are marked as home categories.

    @GetMapping("/getHomeOrAllCategories")
    public ResponseEntity<FmApiResponse<List<FmCreateCategoryResponseDto>>> getHomeOrAllCategories(
            @Parameter(description = "Filter categories. Allowed values: ALL or HOME",
                    example = "ALL")
            @RequestParam String filter) {

        log.info("GET_HOME_OR_ALL_CATEGORIES_API_STARTED | filter={}", filter);

        List<FmCreateCategoryResponseDto> categories =
                categoryService.getHomeOrAllCategories(filter);

        log.info("GET_HOME_OR_ALL_CATEGORIES_API_COMPLETED | totalCategories={}",
                categories.size());

        return ResponseEntity.ok(
                FmApiResponse.success("Categories fetched successfully", categories));
    }


}