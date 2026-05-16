package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.dto.AvailabilityActionRequestDto;
import com.jippy.foodandmart.dto.CreateOutletUnavailabilityRequestDto;
import com.jippy.foodandmart.entity.OutletUnavailability;
import com.jippy.foodandmart.exception.OutletUnavailabilityException;
import com.jippy.foodandmart.mapper.OutletUnavailabilityMapper;
import com.jippy.foodandmart.repository.FmOutletCategoryRepository;
import com.jippy.foodandmart.repository.FmOutletRepository;
import com.jippy.foodandmart.repository.FmProductRepository;
import com.jippy.foodandmart.repository.OutletUnavailabilityRepository;
import com.jippy.foodandmart.service.OutletUnavailabilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutletUnavailabilityServiceImpl implements OutletUnavailabilityService {

    private final OutletUnavailabilityRepository repository;

    private final FmProductRepository productRepository;

    private final FmOutletRepository outletRepository;

    private final FmOutletCategoryRepository outletCategoryRepository;

    private final OutletUnavailabilityMapper unavailabilityMapper;

    @Override
    @Transactional
    public void createUnavailability(CreateOutletUnavailabilityRequestDto requestDto) {

        log.info("CREATE_UNAVAILABILITY_SERVICE START | type={}, unavailabilityId={}, fromDate={}, toDate={}", requestDto.getType(), requestDto.getUnavailabilityId(), requestDto.getUnavailabilityFromDate(), requestDto.getUnavailabilityToDate());

        validateType(requestDto.getType());

        validateReferenceId(requestDto.getType(), requestDto.getUnavailabilityId());

        /*
         * PERMANENT CLOSE FLOW
         */
        if (requestDto.getUnavailabilityFromDate() == null && requestDto.getUnavailabilityToDate() == null) {

            log.info("PERMANENT_CLOSE FLOW START | type={}, unavailabilityId={}", requestDto.getType(), requestDto.getUnavailabilityId());

            validatePermanentCloseReason(requestDto.getReason());

            permanentlyCloseEntity(requestDto.getType(), requestDto.getUnavailabilityId());

            log.info("PERMANENT_CLOSE FLOW SUCCESS | type={}, unavailabilityId={}", requestDto.getType(), requestDto.getUnavailabilityId());

            return;
        }

        /*
         * TEMPORARY CLOSE VALIDATION
         */
        if (requestDto.getUnavailabilityFromDate() == null || requestDto.getUnavailabilityToDate() == null) {

            log.warn("TEMPORARY_CLOSE_VALIDATION FAILED | fromDate={}, toDate={}", requestDto.getUnavailabilityFromDate(), requestDto.getUnavailabilityToDate());

            throw new OutletUnavailabilityException("Unavailability from date and to date are required");
        }

        /*
         * TEMPORARY CLOSE FLOW
         */
        log.info("TEMPORARY_UNAVAILABILITY FLOW START | type={}, unavailabilityId={}", requestDto.getType(), requestDto.getUnavailabilityId());

        validateDateRange(requestDto.getUnavailabilityFromDate(), requestDto.getUnavailabilityToDate());

        OutletUnavailability entity = repository.findActiveUnavailability(requestDto.getType().toUpperCase(), requestDto.getUnavailabilityId(), LocalDateTime.now()).map(existingEntity -> {

            log.info("EXISTING_UNAVAILABILITY_FOUND | outletUnavailabilityId={}, type={}, unavailabilityId={}", existingEntity.getOutletUnavailabilityId(), requestDto.getType(), requestDto.getUnavailabilityId());

            return unavailabilityMapper.updateEntity(existingEntity, requestDto);
        }).orElseGet(() -> {

            log.info("NEW_UNAVAILABILITY_RECORD | type={}, unavailabilityId={}", requestDto.getType(), requestDto.getUnavailabilityId());

            return unavailabilityMapper.mapToEntity(requestDto);
        });

        try {

            repository.save(entity);

            log.info("OUTLET_UNAVAILABILITY_SAVE SUCCESS | outletUnavailabilityId={}, type={}, unavailabilityId={}", entity.getOutletUnavailabilityId(), entity.getType(), entity.getUnavailabilityId());

        } catch (Exception ex) {

            log.error("OUTLET_UNAVAILABILITY_SAVE FAILED | type={}, unavailabilityId={}", requestDto.getType(), requestDto.getUnavailabilityId(), ex);

            throw new OutletUnavailabilityException("Unable to save outlet unavailability");
        }

        updateToggle(requestDto.getType(), requestDto.getUnavailabilityId());

        log.info("TEMPORARY_UNAVAILABILITY FLOW SUCCESS | type={}, unavailabilityId={}", requestDto.getType(), requestDto.getUnavailabilityId());
    }

    @Override
    @Transactional
    public void restoreAvailability(AvailabilityActionRequestDto requestDto) {

        log.info("RESTORE_AVAILABILITY START | type={}, unavailabilityId={}", requestDto.getType(), requestDto.getUnavailabilityId());

        validateType(requestDto.getType());

        OutletUnavailability unavailability = fetchUnavailabilityRecord(requestDto.getType(), requestDto.getUnavailabilityId());

        restoreToggle(requestDto.getType(), requestDto.getUnavailabilityId());

        deleteUnavailabilityRecord(unavailability.getOutletUnavailabilityId());

        log.info("RESTORE_AVAILABILITY SUCCESS | type={}, unavailabilityId={}", requestDto.getType(), requestDto.getUnavailabilityId());
    }

    private void validateType(String type) {

        log.debug("VALIDATE_TYPE START | type={}", type);

        if (type == null || type.isBlank()) {

            log.warn("VALIDATE_TYPE FAILED | type is null or blank");

            throw new OutletUnavailabilityException(FmAppConstants.MSG_INVALID_TYPE);
        }

        String normalizedType = type.toUpperCase();

        if (!normalizedType.equals(FmAppConstants.PRODUCT) && !normalizedType.equals(FmAppConstants.OUTLET) && !normalizedType.equals(FmAppConstants.OUTLET_CATEGORY)) {

            log.warn("VALIDATE_TYPE FAILED | invalid type={}", type);

            throw new OutletUnavailabilityException(FmAppConstants.MSG_INVALID_TYPE);
        }

        log.debug("VALIDATE_TYPE SUCCESS | type={}", type);
    }

    private void validateDateRange(LocalDateTime fromDate, LocalDateTime toDate) {

        log.debug("DATE_RANGE_VALIDATION START | fromDate={}, toDate={}", fromDate, toDate);

        LocalDateTime currentDateTime = LocalDateTime.now().minusSeconds(5);

        log.debug("Fetched currentDateTime successfully | currentDateTime={}", currentDateTime);

        if (fromDate.isBefore(currentDateTime)) {

            log.warn("FROM_DATE_VALIDATION FAILED | fromDate={}, currentDateTime={}", fromDate, currentDateTime);

            throw new OutletUnavailabilityException("Unavailability from date must be present or future");
        }

        if (!toDate.isAfter(fromDate)) {

            log.warn("TO_DATE_VALIDATION FAILED | fromDate={}, toDate={}", fromDate, toDate);

            throw new OutletUnavailabilityException(FmAppConstants.MSG_INVALID_DATE_RANGE);
        }

        log.debug("DATE_RANGE_VALIDATION SUCCESS | fromDate={}, toDate={}", fromDate, toDate);
    }

    private void validateReferenceId(String type, Integer id) {

        log.debug("VALIDATE_REFERENCE_ID START | type={}, id={}", type, id);

        switch (type.toUpperCase()) {

            case FmAppConstants.PRODUCT -> {

                boolean exists = productRepository.existsByProductId(id);

                if (!exists) {

                    log.warn("PRODUCT_NOT_FOUND | productId={}", id);

                    throw new OutletUnavailabilityException(FmAppConstants.MSG_PRODUCT_NOT_FOUND);
                }
            }

            case FmAppConstants.OUTLET -> {

                boolean exists = outletRepository.existsByOutletId(id);

                if (!exists) {

                    log.warn("OUTLET_NOT_FOUND | outletId={}", id);

                    throw new OutletUnavailabilityException(FmAppConstants.MSG_OUTLET_NOT_FOUND);
                }
            }

            case FmAppConstants.OUTLET_CATEGORY -> {

                boolean exists = outletCategoryRepository.existsByOutletCategoryId(id);

                if (!exists) {

                    log.warn("OUTLET_CATEGORY_NOT_FOUND | outletCategoryId={}", id);

                    throw new OutletUnavailabilityException(FmAppConstants.MSG_OUTLET_CATEGORY_NOT_FOUND);
                }
            }

            default -> {

                log.warn("VALIDATE_REFERENCE_ID FAILED | invalid type={}, id={}", type, id);

                throw new OutletUnavailabilityException(FmAppConstants.MSG_INVALID_TYPE);
            }
        }

        log.debug("VALIDATE_REFERENCE_ID SUCCESS | type={}, id={}", type, id);
    }

    private void validatePermanentCloseReason(String reason) {

        log.debug("VALIDATE_PERMANENT_CLOSE_REASON START");

        if (reason == null || reason.isBlank()) {

            log.warn("VALIDATE_PERMANENT_CLOSE_REASON FAILED");

            throw new OutletUnavailabilityException("Reason is required for permanent close");
        }

        log.debug("VALIDATE_PERMANENT_CLOSE_REASON SUCCESS");
    }

    private void updateToggle(String type, Integer id) {

        log.debug("UPDATE_TOGGLE START | type={}, id={}", type, id);

        try {

            switch (type.toUpperCase()) {

                case FmAppConstants.PRODUCT -> {

                    productRepository.disableProduct(id);

                    log.info("PRODUCT_DISABLE SUCCESS | productId={}", id);
                }

                case FmAppConstants.OUTLET -> {

                    outletRepository.disableOutlet(id);

                    log.info("OUTLET_DISABLE SUCCESS | outletId={}", id);
                }

                case FmAppConstants.OUTLET_CATEGORY -> {

                    outletCategoryRepository.disableOutletCategory(id);

                    log.info("OUTLET_CATEGORY_DISABLE SUCCESS | outletCategoryId={}", id);
                }

                default -> {

                    throw new OutletUnavailabilityException(FmAppConstants.MSG_INVALID_TYPE);
                }
            }

        } catch (Exception ex) {

            log.error("UPDATE_TOGGLE FAILED | type={}, id={}", type, id, ex);

            throw new OutletUnavailabilityException("Unable to update availability toggle");
        }

        log.debug("UPDATE_TOGGLE SUCCESS | type={}, id={}", type, id);
    }

    private void permanentlyCloseEntity(String type, Integer id) {

        log.debug("PERMANENT_CLOSE_ENTITY START | type={}, id={}", type, id);

        try {

            switch (type.toUpperCase()) {

                case FmAppConstants.PRODUCT -> {

                    productRepository.permanentlyCloseProduct(id, FmAppConstants.FLAG_NO);

                    log.info("PRODUCT_PERMANENT_CLOSE SUCCESS | productId={}", id);
                }

                case FmAppConstants.OUTLET -> {

                    outletRepository.permanentlyCloseOutlet(id, FmAppConstants.FLAG_NO);

                    log.info("OUTLET_PERMANENT_CLOSE SUCCESS | outletId={}", id);
                }

                case FmAppConstants.OUTLET_CATEGORY -> {

                    outletCategoryRepository.permanentlyCloseOutletCategory(id, FmAppConstants.FLAG_NO);

                    log.info("OUTLET_CATEGORY_PERMANENT_CLOSE SUCCESS | outletCategoryId={}", id);
                }

                default -> {

                    throw new OutletUnavailabilityException(FmAppConstants.MSG_INVALID_TYPE);
                }
            }

        } catch (Exception ex) {

            log.error("PERMANENT_CLOSE_ENTITY FAILED | type={}, id={}", type, id, ex);

            throw new OutletUnavailabilityException("Unable to permanently close entity");
        }

        log.debug("PERMANENT_CLOSE_ENTITY SUCCESS | type={}, id={}", type, id);
    }

    private OutletUnavailability fetchUnavailabilityRecord(String type, Integer unavailabilityId) {

        log.debug("FETCH_UNAVAILABILITY_RECORD START | type={}, unavailabilityId={}", type, unavailabilityId);

        return repository.findActiveUnavailability(type.toUpperCase(), unavailabilityId, LocalDateTime.now()).orElseThrow(() -> {

            log.warn("UNAVAILABILITY_RECORD_NOT_FOUND | type={}, unavailabilityId={}", type, unavailabilityId);

            return new OutletUnavailabilityException("Unavailability record not found");
        });
    }

    private void restoreToggle(String type, Integer id) {

        log.debug("RESTORE_TOGGLE START | type={}, id={}", type, id);

        try {

            switch (type.toUpperCase()) {

                case FmAppConstants.PRODUCT -> {

                    productRepository.enableProduct(id);

                    log.info("PRODUCT_ENABLE SUCCESS | productId={}", id);
                }

                case FmAppConstants.OUTLET -> {

                    outletRepository.enableOutlet(id);

                    log.info("OUTLET_ENABLE SUCCESS | outletId={}", id);
                }

                case FmAppConstants.OUTLET_CATEGORY -> {

                    outletCategoryRepository.enableOutletCategory(id);

                    log.info("OUTLET_CATEGORY_ENABLE SUCCESS | outletCategoryId={}", id);
                }

                default -> {

                    throw new OutletUnavailabilityException(FmAppConstants.MSG_INVALID_TYPE);
                }
            }

        } catch (Exception ex) {

            log.error("RESTORE_TOGGLE FAILED | type={}, id={}", type, id, ex);

            throw new OutletUnavailabilityException("Unable to restore availability");
        }

        log.debug("RESTORE_TOGGLE SUCCESS | type={}, id={}", type, id);
    }

    private void deleteUnavailabilityRecord(Integer outletUnavailabilityId) {

        log.debug("DELETE_UNAVAILABILITY_RECORD START | outletUnavailabilityId={}", outletUnavailabilityId);

        try {

            repository.deleteById(outletUnavailabilityId);

            log.info("DELETE_UNAVAILABILITY_RECORD SUCCESS | outletUnavailabilityId={}", outletUnavailabilityId);

        } catch (Exception ex) {

            log.error("DELETE_UNAVAILABILITY_RECORD FAILED | outletUnavailabilityId={}", outletUnavailabilityId, ex);

            throw new OutletUnavailabilityException("Unable to delete unavailability record");
        }
    }
}