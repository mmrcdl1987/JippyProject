package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.FmBulkUploadResultDTO;
import com.jippy.foodandmart.dto.FmMerchantWithBankDto;
import com.jippy.foodandmart.dto.FmMerchantRequestDTO;
import com.jippy.foodandmart.entity.FmMerchant;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IFmMerchantService {

    List<FmMerchant> getAllMerchants();

    FmMerchant getMerchantById(Integer id);

    long countMerchants();

    FmMerchant createMerchant(FmMerchantRequestDTO dto);

    FmBulkUploadResultDTO bulkUpload(MultipartFile file);
    // Get--> merchant + bank
    FmMerchantWithBankDto getMerchantWithBank(Long merchantId);

    // Update--> merchant + bank
    FmMerchantWithBankDto updateMerchantProfile(FmMerchantWithBankDto dto);
}
