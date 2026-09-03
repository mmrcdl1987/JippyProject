package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.*;
import com.jippy.foodandmart.entity.FmMerchant;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IFmMerchantService {

    List<FmMerchant> getAllMerchants();

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

    FmResponseDto updateMerchantProfilePic(FmMerchantDto merchantDto);

    FmMerchant createMerchantBulkUpload(FmMerchantRequestDTO dto);
}
