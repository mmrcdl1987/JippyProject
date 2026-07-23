package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.dto.PromotionPlanTypeRequestDto;
import com.jippy.foodandmart.dto.PromotionPlanTypeAuditResponseDto;
import com.jippy.foodandmart.dto.PromotionPlanTypeResponseDto;
import com.jippy.foodandmart.entity.PromotionPlanType;
import com.jippy.foodandmart.exception.PromotionPlanTypeAlreadyExistsException;
import com.jippy.foodandmart.exception.ResourceNotFoundException;
import com.jippy.foodandmart.mapper.PromotionPlanTypeMapper;
import com.jippy.foodandmart.repository.PromotionPlanTypeRepository;
import com.jippy.foodandmart.service.IPromotionPlanTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromotionPlanTypeServiceImpl implements IPromotionPlanTypeService {

    private final PromotionPlanTypeRepository promotionPlanTypeRepository;
    private final PromotionPlanTypeMapper promotionPlanTypeMapper;

    @Override
    public PromotionPlanTypeAuditResponseDto createPromotionPlanType(
            PromotionPlanTypeRequestDto requestDto) {

        log.info("[PROMOTION-PLAN-TYPE] Create request received | planName={}",
                requestDto.getPlanName());

        promotionPlanTypeRepository.findByPlanNameIgnoreCase(requestDto.getPlanName().trim())
                .ifPresent(planType -> {
                    log.warn("[PROMOTION-PLAN-TYPE] Duplicate plan type creation attempted | planName={}",
                            requestDto.getPlanName());

                    throw new PromotionPlanTypeAlreadyExistsException(
                            "Promotion plan type already exists with name: "
                                    + requestDto.getPlanName());
                });

        PromotionPlanType entity = promotionPlanTypeMapper.toEntity(requestDto);

        entity.setPlanName(requestDto.getPlanName().trim());
        entity.setCreatedBy(1);
        entity.setCreatedAt(LocalDateTime.now());

        PromotionPlanType savedEntity = promotionPlanTypeRepository.save(entity);

        log.info("[PROMOTION-PLAN-TYPE] Created successfully | id={} | planName={}",
                savedEntity.getPromotionPlanTypesId(),
                savedEntity.getPlanName());

        return promotionPlanTypeMapper.toAuditResponseDto(savedEntity);
    }

    @Override
    public PromotionPlanTypeResponseDto getPromotionPlanTypeById(
            Integer promotionPlanTypeId) {

        log.info("[PROMOTION-PLAN-TYPE] Fetch request | id={}", promotionPlanTypeId);

        PromotionPlanType entity = promotionPlanTypeRepository.findById(promotionPlanTypeId)
                .orElseThrow(() -> {
                    log.warn("[PROMOTION-PLAN-TYPE] Not found | id={}", promotionPlanTypeId);

                    return new ResourceNotFoundException(
                            "Promotion plan type not found with id: "
                                    + promotionPlanTypeId);
                });

        log.info("[PROMOTION-PLAN-TYPE] Fetch successful | id={} | planName={}",
                entity.getPromotionPlanTypesId(),
                entity.getPlanName());

        return promotionPlanTypeMapper.toResponseDto(entity);
    }


    @Override
    public List<PromotionPlanTypeResponseDto> getAllPromotionPlanTypes(){
        log.info("[PROMOTION-PLAN-TYPE] Fetch all request");

        List<PromotionPlanType> entities = promotionPlanTypeRepository.findAll();

        log.info("[PROMOTION-PLAN-TYPE] Total promotion plan types found={}",
                entities.size());

        return entities.stream()
                .map(promotionPlanTypeMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public PromotionPlanTypeAuditResponseDto updatePromotionPlanType(
            Integer promotionPlanTypeId,
            PromotionPlanTypeRequestDto requestDto) {

        log.info("[PROMOTION-PLAN-TYPE] Update request | id={} | newPlanName={}",
                promotionPlanTypeId,
                requestDto.getPlanName());

        PromotionPlanType entity = promotionPlanTypeRepository.findById(promotionPlanTypeId)
                .orElseThrow(() -> {
                    log.warn("[PROMOTION-PLAN-TYPE] Update failed. Not found | id={}",
                            promotionPlanTypeId);

                    return new ResourceNotFoundException(
                            "Promotion plan type not found with id: "
                                    + promotionPlanTypeId);
                });

        promotionPlanTypeRepository
                .findByPlanNameIgnoreCaseAndPromotionPlanTypesIdNot(
                        requestDto.getPlanName().trim(),
                        promotionPlanTypeId)
                .ifPresent(planType -> {
                    log.warn("[PROMOTION-PLAN-TYPE] Duplicate plan name during update | id={} | planName={}",
                            promotionPlanTypeId,
                            requestDto.getPlanName());

                    throw new PromotionPlanTypeAlreadyExistsException(
                            "Promotion plan type already exists with name: "
                                    + requestDto.getPlanName());
                });

        promotionPlanTypeMapper.updateEntity(entity, requestDto);

        entity.setPlanName(requestDto.getPlanName().trim());
        entity.setUpdatedBy(1);
        entity.setUpdatedAt(LocalDateTime.now());

        PromotionPlanType updatedEntity = promotionPlanTypeRepository.save(entity);

        log.info("[PROMOTION-PLAN-TYPE] Updated successfully | id={} | planName={}",
                updatedEntity.getPromotionPlanTypesId(),
                updatedEntity.getPlanName());

        return promotionPlanTypeMapper.toAuditResponseDto(updatedEntity);
    }

    @Override
    public void deletePromotionPlanType(Integer promotionPlanTypeId) {

        log.info("[PROMOTION-PLAN-TYPE] Delete request | id={}", promotionPlanTypeId);

        PromotionPlanType entity = promotionPlanTypeRepository.findById(promotionPlanTypeId)
                .orElseThrow(() -> {
                    log.warn("[PROMOTION-PLAN-TYPE] Delete failed. Not found | id={}",
                            promotionPlanTypeId);

                    return new ResourceNotFoundException(
                            "Promotion plan type not found with id: "
                                    + promotionPlanTypeId);
                });

        promotionPlanTypeRepository.delete(entity);

        log.info("[PROMOTION-PLAN-TYPE] Deleted successfully | id={} | planName={}",
                entity.getPromotionPlanTypesId(),
                entity.getPlanName());
    }
}