package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.dto.FmAreaDto;
import com.jippy.foodandmart.dto.FmOutletsResponseDto;
import com.jippy.foodandmart.dto.FmProductResponseDto;
import com.jippy.foodandmart.entity.FmArea;
import com.jippy.foodandmart.entity.FmOutlet;
import com.jippy.foodandmart.entity.FmProduct;
import com.jippy.foodandmart.mapper.FmMerchantSettlementMapper;
import com.jippy.foodandmart.projections.FmOutletSettlementProjection;
import com.jippy.foodandmart.repository.FmAreaRepository;
import com.jippy.foodandmart.repository.FmOutletRepository;
import com.jippy.foodandmart.repository.FmProductRepository;
import com.jippy.foodandmart.service.FmMerchantSettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FmMerchantSettlementServiceImpl implements FmMerchantSettlementService {

    private final FmProductRepository fmProductsRepository;

    private final FmOutletRepository fmOutletsRepository;

    private final FmAreaRepository fmAreaRepository;

    /*
     Fetch product details using product id
     */
    @Override
    public FmProductResponseDto getProductById(Integer productId) {

        log.info("Fetching product details for product id : {}", productId);

        FmProduct product = fmProductsRepository.findById(productId).orElseThrow(() -> new RuntimeException("Product not found with id : " + productId));

        FmProductResponseDto dto = new FmProductResponseDto();

        dto.setProductId(product.getProductId());

        dto.setProductName(product.getProductName());

        return dto;
    }

//   Fetch outlet details using outlet id
    @Override
    public FmOutletsResponseDto getOutletById(Integer outletId) {

        log.info("Fetching outlet details for outlet id : {}", outletId);

        FmOutletSettlementProjection outlet =
                fmOutletsRepository.getOutletDetailsAndAreaAddressForSettlement(outletId);

        return FmMerchantSettlementMapper.toOutletAndAddressAreaResponseDto(outlet);

    }

//     Fetch area details using area id, this area id
//     is used in settlement process to fetch area details from Area microservice
    @Override
    public FmAreaDto getAreaById(Integer areaId) {

        log.info("Fetching area details for area id : {}", areaId);

        FmArea area = fmAreaRepository.findById(areaId).orElseThrow(() -> new RuntimeException("Area not found"));

        FmAreaDto dto = new FmAreaDto();

        dto.setAreaId(area.getAreaId());

        dto.setAreaName(area.getAreaName());

        return dto;
    }
}