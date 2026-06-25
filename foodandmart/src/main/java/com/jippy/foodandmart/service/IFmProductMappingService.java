package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.*;

public interface IFmProductMappingService {

    FmMapToProductResult mapToProducts(FmMapToProductRequest req);

    FmMasterProductMappingResultDTO mapFromMasterByCategory(Integer outletCategoryId);

    FmCreateMasterProductResponseDto createMasterProduct(FmCreateMasterProductRequestDto request);
}
