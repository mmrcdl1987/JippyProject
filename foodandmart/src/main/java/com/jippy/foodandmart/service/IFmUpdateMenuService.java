package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.FmOutletCategoryDTO;
import com.jippy.foodandmart.dto.FmUpdateMenuResultDTO;

import java.util.List;
import java.util.Map;

public interface IFmUpdateMenuService {

    List<FmOutletCategoryDTO> getMenuByOutlet(Integer outletId);

    FmUpdateMenuResultDTO uploadMenu(List<Map<String, String>> rows, Integer outletId);

    /**
     * Maps (price-updates only) existing outlet products from a CSV/Excel upload.
     *
     * <p>Matches each row by category name + product name against the outlet's
     * existing catalogue. Only {@code merchantPrice} is updated — no new products
     * or categories are created. Unmatched rows are reported as errors.</p>
     *
     * @param rows     parsed rows (normalised column → value), must contain
     *                 {@code category}, {@code productname}, and {@code price}
     *                 (or {@code merchantprice}) columns
     * @param outletId the outlet whose products should be updated
     * @return an {@link FmUpdateMenuResultDTO} with per-row results
     */
    FmUpdateMenuResultDTO mapMenuFromCsv(List<Map<String, String>> rows, Integer outletId);
}
