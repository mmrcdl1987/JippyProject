package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.*;
import com.jippy.foodandmart.entity.FmMerchant;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IFmMerchantService {

    List<FmMerchantDto> getAllMerchants();

    FmMerchantDto getMerchantById(Integer id);

    long countMerchants();

    FmMerchant createMerchant(FmMerchantRequestDTO dto);

    FmMerchant createMerchant(FmMerchantRequestDTO dto, MultipartFile aadharFile, MultipartFile panFile);

    FmBulkUploadResultDTO bulkUpload(MultipartFile file);
    // Get--> merchant + bank
    FmMerchantWithBankDto getMerchantWithBank(Integer merchantId);

    // Update--> merchant + bank
    FmMerchantWithBankDto updateMerchantProfile(FmMerchantWithBankDto dto);

    FmMerchantWithBankDto updateMerchantProfile(FmMerchantWithBankDto dto,
                                                MultipartFile aadharFile, MultipartFile panFile);

    FmResponseDto updateMerchantProfilePic(Integer merchantId, MultipartFile file);
    FmResponseDto toggleMerchant(FmToggleMerchantRequestDto requestDto);

    FmMerchant createMerchantBulkUpload(FmMerchantRequestDTO dto);

    FmMerchantAddressDto getMerchantAddress(Integer merchantId);
}
