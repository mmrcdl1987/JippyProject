package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.dto.FmOutletTransferRequestDTO;
import com.jippy.foodandmart.dto.FmOutletTransferResponseDTO;
import com.jippy.foodandmart.entity.FmMerchant;
import com.jippy.foodandmart.entity.FmOutlet;
import com.jippy.foodandmart.repository.FmMerchantRepository;
import com.jippy.foodandmart.repository.FmOutletRepository;
import com.jippy.foodandmart.service.IFmOutletTransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service implementation for transferring an outlet from one merchant to another.
 *
 * <p>The FmOutletTransferHistory entity has been removed. Transfer still updates
 * the outlet's merchantId but no longer persists a history record.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FmOutletTransferServiceImpl implements IFmOutletTransferService {

    private final FmOutletRepository outletRepository;
    private final FmMerchantRepository merchantRepository;

    @Override
    @Transactional
    public FmOutletTransferResponseDTO transferOutlet(FmOutletTransferRequestDTO request) {

        FmOutlet outlet = outletRepository.findById(request.getOutletId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Outlet ID " + request.getOutletId() + " does not exist"));

        if (!FmAppConstants.FLAG_YES.equalsIgnoreCase(outlet.getIsActive()))
            throw new IllegalStateException("Cannot transfer an inactive outlet (outletId=" + outlet.getOutletId() + ")");

        Integer fromMerchantId = outlet.getMerchantId();

        FmMerchant toMerchant = merchantRepository.findById(request.getToMerchantId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Target merchant ID " + request.getToMerchantId() + " does not exist"));

        if (!FmAppConstants.FLAG_YES.equalsIgnoreCase(toMerchant.getIsActive()))
            throw new IllegalStateException("Target merchant is inactive (merchantId=" + toMerchant.getMerchantId() + ")");

        if (fromMerchantId.equals(request.getToMerchantId()))
            throw new IllegalArgumentException("Outlet already belongs to merchant ID " + fromMerchantId);

        FmMerchant fromMerchant = merchantRepository.findById(fromMerchantId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Current owner merchant ID " + fromMerchantId + " not found"));

        log.info("[TRANSFER] Transferring outletId={} from merchantId={} to merchantId={}",
                outlet.getOutletId(), fromMerchantId, request.getToMerchantId());

        outlet.setMerchantId(request.getToMerchantId());
        outlet.setUpdatedAt(LocalDateTime.now());
        outlet.setUpdatedBy(request.getTransferredBy());
        outletRepository.save(outlet);

        FmOutletTransferResponseDTO response = new FmOutletTransferResponseDTO();
        response.setTransferId(null); // No history table — no transfer ID
        response.setOutletId(outlet.getOutletId());
        response.setOutletName(outlet.getOutletName());
        response.setFromMerchantId(fromMerchantId);
        response.setFromMerchantName(fromMerchant.getMerchantName());
        response.setToMerchantId(toMerchant.getMerchantId());
        response.setToMerchantName(toMerchant.getMerchantName());
        response.setTransferReason(request.getTransferReason());
        response.setTransferStatus(FmAppConstants.TRANSFER_STATUS_COMPLETED);
        response.setTransferredAt(LocalDateTime.now());
        return response;
    }

    @Override
    public List<FmOutletTransferResponseDTO> getHistoryByOutlet(Integer outletId) {
        log.warn("[TRANSFER] getHistoryByOutlet called but transfer history table has been removed.");
        return List.of();
    }

    @Override
    public List<FmOutletTransferResponseDTO> getInboundTransfers(Integer merchantId) {
        log.warn("[TRANSFER] getInboundTransfers called but transfer history table has been removed.");
        return List.of();
    }

    @Override
    public List<FmOutletTransferResponseDTO> getOutboundTransfers(Integer merchantId) {
        log.warn("[TRANSFER] getOutboundTransfers called but transfer history table has been removed.");
        return List.of();
    }

    @Override
    public List<FmOutletTransferResponseDTO> getAllTransfers() {
        log.warn("[TRANSFER] getAllTransfers called but transfer history table has been removed.");
        return List.of();
    }
}
