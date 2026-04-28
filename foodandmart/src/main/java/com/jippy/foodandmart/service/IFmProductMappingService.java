package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.FmMapToProductRequest;
import com.jippy.foodandmart.dto.FmMapToProductResult;
import com.jippy.foodandmart.dto.FmMasterProductMappingResultDTO;

public interface IFmProductMappingService {

    FmMapToProductResult mapToProducts(FmMapToProductRequest req);

    FmMasterProductMappingResultDTO mapFromMasterByCategory(Integer outletCategoryId);
}
