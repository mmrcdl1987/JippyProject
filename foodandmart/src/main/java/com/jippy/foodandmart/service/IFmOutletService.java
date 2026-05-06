package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.FmCustomerNearbyResponseDto;
import com.jippy.foodandmart.dto.*;
import com.jippy.foodandmart.entity.FmOutlet;

import java.util.List;

public interface IFmOutletService {

    long countOutlets();

    List<FmOutlet> getAllOutlets();

    List<FmOutletSummaryDTO> getAllOutletsSummary();

    List<FmOutletSummaryDTO> getOutletsByMerchantId(Integer merchantId);

    FmOutlet getOutletById(Integer id);

    FmOutletCreatedDTO createOutlet(FmOutletRequestDTO dto);

    //FmBulkOutletResultDTO bulkUpload(List<FmOutletRequestDTO> rows);




    //     for api to get outlet details by outlet id and user type (merchant or customer)
    FmOutletDetailsDto getOutletDetails(Integer outletId, String userType);

    //    for api to get all outlets by merchant id
   List<FmOutletByMerchantDto> getOutletsByFmMerchantId(Integer merchantId);

    //    for update outlet details by outlet id
    FmOutletDetailsDto updateOutletDetails(Integer outletId, FmOutletDetailsDto dto, String userType);
    /**
     * Customer App: fetch active outlets within default 3 km of the customer.
     * Always uses DEFAULT_RADIUS_KM = 3.0 km — no caller override.
     */
    FmCustomerNearbyResponseDto fetchCustomerNearbyOutlets(double customerLat, double customerLng);
}
