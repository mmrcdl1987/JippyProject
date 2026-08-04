package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.FmCustomerNearbyResponseDto;
import com.jippy.foodandmart.dto.*;
import com.jippy.foodandmart.entity.FmOutlet;

import java.util.List;

public interface IFmOutletService {

    FmOutletCreateResponseDTO createOutlet(FmOutletRequestDTO dto);

//    this updates the outlet details by merchant id and outlet id, and returns the updated outlet details
//    and also updates the outlet bank details if provided in the request dto
//    and also updates the outlet address details if provided in the request dto
//    and also updates the outlet timings if provided in the request dto

    FmUpdateOutletRequestDTO updateOutletDetailsByMerchant(Integer outletId, FmUpdateOutletRequestDTO dto);

    long countOutlets();

    List<FmOutlet> getAllOutlets();

    List<FmOutletSummaryDTO> getAllOutletsSummary();

    List<FmOutletSummaryDTO> getOutletsByMerchantId(Integer merchantId);

//    FmOutlet getOutletById(Integer id);

    FmOutletResponseDto getOutletById(Integer id);

    //    FmOutletCreatedDTO createOutlet(FmOutletRequestDTO dto);
    FmOutletCreatedDTO createOutletForBulkUploadAndOtpValidation(FmOutletRequestDTO dto);

    FmOutletCreatedDTO createOutletBulkUpload(FmOutletRequestDTO dto);

    //FmBulkOutletResultDTO bulkUpload(List<FmOutletRequestDTO> rows);


    //     for api to get outlet details by outlet id and user type (merchant or customer)
    FmOutletDetailsDto getOutletDetails(Integer outletId, String userType, Integer customerId);

    //    for api to get all outlets by merchant id
    List<FmOutletByMerchantDto> getOutletsByFmMerchantId(Integer merchantId);

    //    for update outlet details by outlet id
    FmOutletDetailsDto updateOutletDetails(Integer outletId,
                                           FmOutletDetailsDto dto, String userType);

    /**
     * Customer App: fetch active outlets within default 3 km of the customer.
     * Always uses DEFAULT_RADIUS_KM = 3.0 km — no caller override.
     */
    FmCustomerNearbyResponseDto fetchCustomerNearbyOutlets(double customerLat, double customerLng, Integer categoryId);

    FmAddressRequestDto saveAddressDetails(FmAddressRequestDto fmAddressRequestDto);

    FmAddressRequestDto getAddressDetails(Integer addressId);

    OutletLocationResponseDto getOutletLocation(Integer outletId);

    public String fetchOutletName(Integer outletId);

    List<FmOutlet> getOutletsByAreaId(Integer areaId);
}
