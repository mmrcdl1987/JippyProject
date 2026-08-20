package com.jippy.customerandorder.iservice;

import com.jippy.customerandorder.dto.CoSaveWalletPointsRequestDTO;
import com.jippy.customerandorder.dto.CoSaveWalletPointsResponseDTO;

public interface CoWalletPointsService {

    CoSaveWalletPointsResponseDTO
    saveWalletPointsEqualToOrderAmountDiscounted(CoSaveWalletPointsRequestDTO requestDTO);
}