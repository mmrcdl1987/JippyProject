package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.dto.FmProductVariantGroupValueResponseDto;
import com.jippy.foodandmart.dto.FmProductVariantValueRequestDto;
import com.jippy.foodandmart.entity.FmProductVariantGroup;
import com.jippy.foodandmart.entity.FmProductVariantGroupValue;
import com.jippy.foodandmart.exception.DuplicateResourceException;
import com.jippy.foodandmart.exception.ResourceNotFoundException;
import com.jippy.foodandmart.mapper.FmProductVariantGroupValueMapper;
import com.jippy.foodandmart.repository.FmProductVariantGroupRepository;
import com.jippy.foodandmart.repository.FmProductVariantGroupValueRepository;
import com.jippy.foodandmart.service.IFmProductVariantGroupValueService;
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
public class FmProductVariantGroupValueServiceImpl implements IFmProductVariantGroupValueService {

    private final FmProductVariantGroupRepository productVariantGroupRepository;

    private final FmProductVariantGroupValueRepository productVariantGroupValueRepository;

    @Override
    public FmProductVariantGroupValueResponseDto saveVariantGroupValue(Integer productVariantGroupsId, FmProductVariantValueRequestDto request) {

        log.info("Save Product Variant Value request received. GroupId={}, VariantName={}", productVariantGroupsId, request.getVariantName());

        validateGroupExists(productVariantGroupsId);

        validateRequest(productVariantGroupsId, request);

        if (request.getProductVariantGroupValuesId() == null) {
            return createVariantGroupValue(productVariantGroupsId, request);
        }

        return updateVariantGroupValue(productVariantGroupsId, request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FmProductVariantGroupValueResponseDto> getVariantGroupValues(Integer productVariantGroupsId) {

        log.info("Fetching variant values. GroupId={}", productVariantGroupsId);

        validateGroupExists(productVariantGroupsId);

        List<FmProductVariantGroupValue> values = productVariantGroupValueRepository.findByProductVariantGroupsIdAndIsActiveTrueOrderByVariantNameAsc(productVariantGroupsId);

        log.info("Fetched {} variant values for GroupId={}", values.size(), productVariantGroupsId);

        return values.stream().map(FmProductVariantGroupValueMapper::toResponseDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public FmProductVariantGroupValueResponseDto getVariantGroupValueById(Integer groupId, Integer valueId) {

        log.info("Fetching variant value. GroupId={}, ValueId={}", groupId, valueId);

        validateGroupExists(groupId);

        FmProductVariantGroupValue entity = productVariantGroupValueRepository.findByProductVariantGroupValuesIdAndProductVariantGroupsIdAndIsActiveTrue(valueId, groupId).orElseThrow(() -> {

            log.warn("Variant value not found. GroupId={}, ValueId={}", groupId, valueId);

            return new ResourceNotFoundException("Product Variant Value not found with id : " + valueId);
        });

        log.info("Variant value found. Id={}, Name={}", entity.getProductVariantGroupValuesId(), entity.getVariantName());

        return FmProductVariantGroupValueMapper.toResponseDto(entity);
    }

    @Override
    public void deleteVariantGroupValue(Integer groupId, Integer valueId) {

        log.info("Deleting variant value. GroupId={}, ValueId={}", groupId, valueId);

        validateGroupExists(groupId);

        FmProductVariantGroupValue entity = productVariantGroupValueRepository.findByProductVariantGroupValuesIdAndProductVariantGroupsIdAndIsActiveTrue(valueId, groupId).orElseThrow(() -> {

            log.warn("Variant value not found. GroupId={}, ValueId={}", groupId, valueId);

            return new ResourceNotFoundException("Product Variant Value not found with id : " + valueId);
        });

        entity.setIsActive(Boolean.FALSE);

        productVariantGroupValueRepository.save(entity);

        log.info("Variant value deleted successfully. Id={}, Name={}", entity.getProductVariantGroupValuesId(), entity.getVariantName());
    }

    /**
     * Create Variant Value
     */
    private FmProductVariantGroupValueResponseDto createVariantGroupValue(Integer productVariantGroupsId, FmProductVariantValueRequestDto request) {

        FmProductVariantGroupValue entity = FmProductVariantGroupValueMapper.toEntity(productVariantGroupsId, request);

        entity = productVariantGroupValueRepository.save(entity);

        log.info("Product Variant Value created successfully. Id={}, VariantName={}", entity.getProductVariantGroupValuesId(), entity.getVariantName());

        return FmProductVariantGroupValueMapper.toResponseDto(entity);
    }

    /**
     * Update Variant Value
     */
    private FmProductVariantGroupValueResponseDto updateVariantGroupValue(Integer productVariantGroupsId, FmProductVariantValueRequestDto request) {

        log.info("Updating Product Variant Value. Id={}", request.getProductVariantGroupValuesId());

        FmProductVariantGroupValue entity = productVariantGroupValueRepository.findByProductVariantGroupValuesIdAndProductVariantGroupsIdAndIsActiveTrue(request.getProductVariantGroupValuesId(), productVariantGroupsId).orElseThrow(() -> {

            log.warn("Product Variant Value not found. Id={}", request.getProductVariantGroupValuesId());

            return new ResourceNotFoundException("Product Variant Value not found with id : " + request.getProductVariantGroupValuesId());
        });

        entity.setVariantName(request.getVariantName().trim());

        entity = productVariantGroupValueRepository.save(entity);

        log.info("Product Variant Value updated successfully. Id={}, VariantName={}", entity.getProductVariantGroupValuesId(), entity.getVariantName());

        return FmProductVariantGroupValueMapper.toResponseDto(entity);
    }

    /**
     * Validate parent Variant Group exists.
     */
    private FmProductVariantGroup validateGroupExists(Integer productVariantGroupsId) {

        FmProductVariantGroup group = productVariantGroupRepository.findByProductVariantGroupsIdAndIsActiveTrue(productVariantGroupsId).orElseThrow(() -> {

            log.warn("Product Variant Group not found. GroupId={}", productVariantGroupsId);

            return new ResourceNotFoundException("Product Variant Group not found with id : " + productVariantGroupsId);
        });

        return group;
    }

    /**
     * Validate duplicate Variant Value.
     */
    private void validateRequest(Integer productVariantGroupsId, FmProductVariantValueRequestDto request) {

        if (request.getVariantName() == null || request.getVariantName().isBlank()) {

            log.error("Variant Name is missing.");

            throw new IllegalArgumentException("Variant Name is required.");
        }

        String variantName = request.getVariantName().trim();

        if (request.getProductVariantGroupValuesId() == null) {

            if (productVariantGroupValueRepository.existsByProductVariantGroupsIdAndVariantNameIgnoreCase(productVariantGroupsId, variantName)) {

                log.warn("Duplicate Product Variant Value. GroupId={}, VariantName={}", productVariantGroupsId, variantName);

                throw new DuplicateResourceException("Product Variant Value '" + variantName + "' already exists.");
            }

        } else {

            if (productVariantGroupValueRepository.existsByProductVariantGroupsIdAndVariantNameIgnoreCaseAndProductVariantGroupValuesIdNot(productVariantGroupsId, variantName, request.getProductVariantGroupValuesId())) {

                log.warn("Duplicate Product Variant Value. GroupId={}, VariantName={}", productVariantGroupsId, variantName);

                throw new DuplicateResourceException("Product Variant Value '" + variantName + "' already exists.");
            }
        }
    }
}