package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.FmOutletTransferRequestDTO;
import com.jippy.foodandmart.dto.FmOutletTransferResponseDTO;

import java.util.List;

public interface IFmOutletTransferService {

    FmOutletTransferResponseDTO transferOutlet(FmOutletTransferRequestDTO request);

    List<FmOutletTransferResponseDTO> getHistoryByOutlet(Integer outletId);

    List<FmOutletTransferResponseDTO> getInboundTransfers(Integer merchantId);

    List<FmOutletTransferResponseDTO> getOutboundTransfers(Integer merchantId);

    List<FmOutletTransferResponseDTO> getAllTransfers();
}
