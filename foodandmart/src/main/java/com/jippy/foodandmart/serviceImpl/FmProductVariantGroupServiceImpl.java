package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.dto.FmProductVariantGroupRequestDto;
import com.jippy.foodandmart.dto.FmProductVariantGroupResponseDto;
import com.jippy.foodandmart.entity.FmProductVariantGroup;
import com.jippy.foodandmart.exception.DuplicateResourceException;
import com.jippy.foodandmart.exception.ResourceNotFoundException;
import com.jippy.foodandmart.mapper.FmProductVariantGroupMapper;
import com.jippy.foodandmart.repository.FmProductVariantGroupRepository;
import com.jippy.foodandmart.service.IFmProductVariantGroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class FmProductVariantGroupServiceImpl implements IFmProductVariantGroupService {

    private static final String SINGLE = "SINGLE";
    private static final String MULTIPLE = "MULTIPLE";

    private final FmProductVariantGroupRepository productVariantGroupRepository;

    @Override
    public FmProductVariantGroupResponseDto saveVariantGroup(
            FmProductVariantGroupRequestDto request) {

        log.info("Save Variant Group Request : {}", request.getGroupName());

        validateRequest(request);

        if (request.getProductVariantGroupsId() == null) {
            return createVariantGroup(request);
        }

        return updateVariantGroup(request);
    }
    @Override
    @Transactional(readOnly = true)
    public List<FmProductVariantGroupResponseDto> getAllVariantGroups() {

        log.info("Fetching all active product variant groups.");

        List<FmProductVariantGroup> groups =
                productVariantGroupRepository.findByIsActiveTrueOrderByDisplayOrderAsc();

        log.info("Fetched {} active product variant groups.", groups.size());

        return groups.stream()
                .map(FmProductVariantGroupMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public FmProductVariantGroupResponseDto getVariantGroupById(Integer groupId) {

        log.info("Fetching product variant group. Id={}", groupId);

        FmProductVariantGroup entity = productVariantGroupRepository
                .findByProductVariantGroupsIdAndIsActiveTrue(groupId)
                .orElseThrow(() -> {

                    log.warn("Product Variant Group not found. Id={}", groupId);

                    return new ResourceNotFoundException(
                            "Product Variant Group not found with id : " + groupId);
                });

        log.info("Product Variant Group found. Id={}, Name={}",
                entity.getProductVariantGroupsId(),
                entity.getGroupName());

        return FmProductVariantGroupMapper.toResponseDto(entity);
    }

    @Override
    public void deleteVariantGroup(Integer groupId) {

        log.info("Deleting product variant group. Id={}", groupId);

        FmProductVariantGroup entity = productVariantGroupRepository
                .findByProductVariantGroupsIdAndIsActiveTrue(groupId)
                .orElseThrow(() -> {

                    log.warn("Product Variant Group not found. Id={}", groupId);

                    return new ResourceNotFoundException(
                            "Product Variant Group not found with id : " + groupId);
                });

        entity.setIsActive(Boolean.FALSE);

        productVariantGroupRepository.save(entity);

        log.info("Product Variant Group deleted successfully. Id={}, Name={}",
                entity.getProductVariantGroupsId(),
                entity.getGroupName());
    }

    /**
     * Create Variant Group
     */
    private FmProductVariantGroupResponseDto createVariantGroup(
            FmProductVariantGroupRequestDto request) {

        String groupName = request.getGroupName().trim();

        if (productVariantGroupRepository.existsByGroupNameIgnoreCase(groupName)) {

            log.warn("Product Variant Group already exists. Name={}", groupName);

            throw new DuplicateResourceException(
                    "Product Variant Group already exists : " + groupName);
        }

        FmProductVariantGroup entity =
                FmProductVariantGroupMapper.toEntity(request);

        entity = productVariantGroupRepository.save(entity);

        log.info("Product Variant Group created successfully. Id={}, Name={}",
                entity.getProductVariantGroupsId(),
                entity.getGroupName());

        return FmProductVariantGroupMapper.toResponseDto(entity);
    }

    /**
     * Update Variant Group
     */
    private FmProductVariantGroupResponseDto updateVariantGroup(
            FmProductVariantGroupRequestDto request) {

        log.info("Updating Product Variant Group. Id={}",
                request.getProductVariantGroupsId());

        FmProductVariantGroup entity = productVariantGroupRepository
                .findByProductVariantGroupsIdAndIsActiveTrue(
                        request.getProductVariantGroupsId())
                .orElseThrow(() -> {

                    log.warn("Product Variant Group not found. Id={}",
                            request.getProductVariantGroupsId());

                    return new ResourceNotFoundException(
                            "Product Variant Group not found with id : "
                                    + request.getProductVariantGroupsId());
                });

        String groupName = request.getGroupName().trim();

        if (productVariantGroupRepository
                .existsByGroupNameIgnoreCaseAndProductVariantGroupsIdNot(
                        groupName,
                        entity.getProductVariantGroupsId())) {

            log.warn("Product Variant Group already exists. Name={}", groupName);

            throw new DuplicateResourceException(
                    "Product Variant Group already exists : " + groupName);
        }

        entity.setGroupName(groupName);
        entity.setSelectionType(request.getSelectionType().trim().toUpperCase());
        entity.setMinSelection(request.getMinSelection());
        entity.setMaxSelection(request.getMaxSelection());
        entity.setDisplayOrder(request.getDisplayOrder());

        entity = productVariantGroupRepository.save(entity);

        log.info("Product Variant Group updated successfully. Id={}, Name={}",
                entity.getProductVariantGroupsId(),
                entity.getGroupName());

        return FmProductVariantGroupMapper.toResponseDto(entity);
    }

    /**
     * Validate request
     */
    private void validateRequest(FmProductVariantGroupRequestDto request) {

        validateSelectionType(request.getSelectionType());

        validateSelectionRange(
                request.getMinSelection(),
                request.getMaxSelection());
    }

    /**
     * Validate Selection Type
     */
    private void validateSelectionType(String selectionType) {

        if (selectionType == null || selectionType.isBlank()) {

            log.error("Selection Type is missing.");

            throw new IllegalArgumentException(
                    "Selection Type is required.");
        }

        String value = selectionType.trim().toUpperCase();

        if (!SINGLE.equals(value) && !MULTIPLE.equals(value)) {

            log.error("Invalid Selection Type received : {}", selectionType);

            throw new IllegalArgumentException(
                    "Selection Type must be SINGLE or MULTIPLE.");
        }
    }

    /**
     * Validate Selection Range
     */
    private void validateSelectionRange(Integer minSelection,
                                        Integer maxSelection) {

        if (minSelection == null) {

            log.error("Minimum Selection is missing.");

            throw new IllegalArgumentException(
                    "Minimum Selection is required.");
        }

        if (maxSelection == null) {

            log.error("Maximum Selection is missing.");

            throw new IllegalArgumentException(
                    "Maximum Selection is required.");
        }

        if (minSelection < 0) {

            log.error("Invalid Minimum Selection : {}", minSelection);

            throw new IllegalArgumentException(
                    "Minimum Selection cannot be less than zero.");
        }

        if (maxSelection <= 0) {

            log.error("Invalid Maximum Selection : {}", maxSelection);

            throw new IllegalArgumentException(
                    "Maximum Selection must be greater than zero.");
        }

        if (minSelection > maxSelection) {

            log.error("Invalid Selection Range. Min={}, Max={}",
                    minSelection,
                    maxSelection);

            throw new IllegalArgumentException(
                    "Minimum Selection cannot be greater than Maximum Selection.");
        }
    }
}