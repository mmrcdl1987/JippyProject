package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.*;
import com.jippy.foodandmart.entity.FmMerchant;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IFmMerchantService {

    List<FmMerchant> getAllMerchants();

    FmMerchantDto getMerchantById(Integer id);

    long countMerchants();

    FmMerchant createMerchant(FmMerchantRequestDTO dto);

    // FmMerchant createMerchant(FmMerchantRequestDTO dto, MultipartFile aadharFile, MultipartFile panFile);

    FmBulkUploadResultDTO bulkUpload(MultipartFile file);
    // Get--> merchant + bank
    FmMerchantWithBankDto getMerchantWithBank(Integer merchantId);

    // Update--> merchant + bank
    FmMerchantWithBankDto updateMerchantProfile(FmMerchantWithBankDto dto);

    FmResponseDto updateMerchantProfilePic(FmMerchantDto merchantDto);

    FmResponseDto toggleMerchant(FmToggleMerchantRequestDto requestDto);

    FmMerchantAddressDto getMerchantAddress(Integer merchantId);

    FmMerchant createMerchantBulkUpload(FmMerchantRequestDTO dto);

    /**
     * Searches merchants using merchant name.
     *
     * @param merchantName partial merchant name
     * @return matching merchants
     */
    List<FmMerchantSearchResponseDto> searchByMerchantName(
            String merchantName
    );

    Page<FmMerchantDto> getAdminMerchants(
            FmMerchantAdminFilterDto filter,
            int page,
            int size
    );

}
