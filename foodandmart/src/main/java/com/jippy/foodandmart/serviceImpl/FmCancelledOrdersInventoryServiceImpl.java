package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.dto.FmAssignCancelledOrderRequestDto;
import com.jippy.foodandmart.dto.FmAssignCancelledOrderResponseDto;
import com.jippy.foodandmart.entity.FmCancelledOrdersInventory;
import com.jippy.foodandmart.repository.FmCancelledOrdersInventoryRepository;
import com.jippy.foodandmart.service.FmCancelledOrdersInventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FmCancelledOrdersInventoryServiceImpl implements FmCancelledOrdersInventoryService {

    private final FmCancelledOrdersInventoryRepository repository;

    @Override
    public FmAssignCancelledOrderResponseDto assignCancelledOrder(FmAssignCancelledOrderRequestDto requestDto) {

        if (repository.existsByCancelledOrderId(requestDto.getOrderId())) {

            throw new RuntimeException("Order already assigned");
        }

        FmCancelledOrdersInventory entity = new FmCancelledOrdersInventory();

        entity.setCancelledOrderId(requestDto.getOrderId());

        entity.setDriverId(requestDto.getDriverId());

        entity.setSpecializedOutletId(requestDto.getSpecializedOutletId());

        entity.setCreatedBy(requestDto.getDriverId());

        entity.setCreatedAt(LocalDateTime.now());

        repository.save(entity);

        return FmAssignCancelledOrderResponseDto.builder().success(true).message("Cancelled order assigned successfully").build();
    }
}