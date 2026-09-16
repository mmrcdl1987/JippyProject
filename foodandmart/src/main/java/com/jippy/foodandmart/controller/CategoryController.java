package com.jippy.foodandmart.controller;
import com.jippy.foodandmart.dto.*;
import com.jippy.foodandmart.service.IFmCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequestMapping("/api/fm")
@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final IFmCategoryService categoryService;



    /**
     * Creates a new global category.
     * <p>
     * This API inserts only into:
     * - jippy_fm.categories
     * <p>
     * Newly created categories will be visible through
     * GET /api/fm/categories after refresh.
     */
    @PostMapping(
            value = "/createCategory",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<FmCreateCategoryResponseDto> createCategory(
            @ModelAttribute FmCreateCategoryRequestDto request) {

        return ResponseEntity.ok(
                categoryService.createCategory(request)
        );
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
    public ResponseEntity<FmApiResponse<FmCategoryFilterResponseDto>> getHomeOrAllCategories(

            @RequestParam(required = false) String filter) {

        log.info("GET_CATEGORY_LIST_API_STARTED | filter={}", filter);

        FmCategoryFilterResponseDto response = categoryService.getHomeOrAllCategories(filter);

        log.info("GET_CATEGORY_LIST_API_COMPLETED | totalCount={} | filteredCount={}", response.getTotalCount(), response.getFilteredCount());

        return ResponseEntity.ok(FmApiResponse.success("Categories fetched successfully", response));
    }




    @PutMapping(
            value = "/updateCategory",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<FmApiResponse<FmCreateCategoryResponseDto>> updateCategory(
            @Valid @ModelAttribute FmUpdateCategoryRequestDto request) {

        log.info(
                "UPDATE_CATEGORY_API_STARTED | categoryId={}",
                request.getCategoryId()
        );

        FmCreateCategoryResponseDto response =
                categoryService.updateCategory(request);

        log.info(
                "UPDATE_CATEGORY_API_COMPLETED | categoryId={}",
                response.getCategoryId()
        );

        return ResponseEntity.ok(
                FmApiResponse.success(
                        "Category updated successfully",
                        response
                )
        );
    }
}