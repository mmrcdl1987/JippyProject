package com.jippy.customerandorder.serviceImpl;
import com.jippy.customerandorder.dto.CustomerDeliveryChargeCalculationResponseDto;
import com.jippy.customerandorder.dto.CustomerDeliveryChargeSettingsDTO;
import com.jippy.customerandorder.entity.CustomerDeliveryChargeSettings;
import com.jippy.customerandorder.iservice.CustomerDeliveryChargeSettingsService;
import com.jippy.customerandorder.repository.CustomerDeliveryChargeSettingsRepository;
import com.jippy.foodandmart.exception.DuplicateResourceException;
import com.jippy.foodandmart.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.RoundingMode;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CustomerDeliveryChargeSettingsServiceImpl implements CustomerDeliveryChargeSettingsService {

    private final CustomerDeliveryChargeSettingsRepository repository;

    @Override
    public CustomerDeliveryChargeSettingsDTO create(CustomerDeliveryChargeSettingsDTO dto, Integer userId) {

        log.info("Creating delivery charge setting for cityId={}, threshold={}", dto.getCityId(), dto.getOrderValueThreshold());

        validateDuplicateThreshold(dto.getCityId(), dto.getOrderValueThreshold());

        CustomerDeliveryChargeSettings entity = new CustomerDeliveryChargeSettings();

        mapDtoToEntity(dto, entity);

        entity.setCreatedAt(LocalDateTime.now());
        entity.setCreatedBy(userId);

        if (entity.getIsActive() == null) {
            entity.setIsActive(true);
        }

        CustomerDeliveryChargeSettings saved = repository.save(entity);

        log.info("Delivery charge setting created successfully, id={}", saved.getCustomerDeliveryChargeSettingsId());

        return mapEntityToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerDeliveryChargeSettingsDTO> getAll() {

        log.info("Fetching all delivery charge settings");

        return repository.findAll().stream().map(this::mapEntityToDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDeliveryChargeSettingsDTO getById(Integer id) {

        log.info("Fetching delivery charge setting by id={}", id);

        CustomerDeliveryChargeSettings entity = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Delivery charge setting not found with id: " + id));

        return mapEntityToDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerDeliveryChargeSettingsDTO> getByCityId(Integer cityId) {

        log.info("Fetching delivery charge settings for cityId={}", cityId);

        return repository.findByCityIdOrderByOrderValueThresholdAsc(cityId).stream().map(this::mapEntityToDto).toList();
    }

    @Override
    public CustomerDeliveryChargeSettingsDTO update(Integer id, CustomerDeliveryChargeSettingsDTO dto, Integer userId) {

        log.info("Updating delivery charge setting id={}", id);

        CustomerDeliveryChargeSettings entity = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Delivery charge setting not found with id: " + id));

        boolean duplicateExists = repository.existsByCityIdAndOrderValueThresholdAndCustomerDeliveryChargeSettingsIdNot(dto.getCityId(), dto.getOrderValueThreshold(), id);

        if (duplicateExists) {
            throw new DuplicateResourceException("A delivery charge setting already exists for cityId " + dto.getCityId() + " and order value threshold " + dto.getOrderValueThreshold());
        }

        mapDtoToEntity(dto, entity);

        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy(userId);

        CustomerDeliveryChargeSettings updated = repository.save(entity);

        log.info("Delivery charge setting updated successfully, id={}", id);

        return mapEntityToDto(updated);
    }

    @Override
    public void delete(Integer id) {

        log.info("Deleting delivery charge setting id={}", id);

        CustomerDeliveryChargeSettings entity = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Delivery charge setting not found with id: " + id));

        repository.delete(entity);

        log.info("Delivery charge setting deleted successfully, id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDeliveryChargeSettingsDTO getApplicablePlan(Integer cityId, BigDecimal orderValue) {

        log.info("Finding applicable delivery plan for cityId={}, orderValue={}", cityId, orderValue);

        CustomerDeliveryChargeSettings entity = repository.findFirstByCityIdAndIsActiveTrueAndOrderValueThresholdLessThanEqualOrderByOrderValueThresholdDesc(cityId, orderValue).orElseThrow(() -> new ResourceNotFoundException("No active delivery charge plan found for cityId: " + cityId + " and order value: " + orderValue));

        return mapEntityToDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDeliveryChargeCalculationResponseDto calculateCustomerDeliveryCharge(
            Integer cityId,
            BigDecimal orderAmountDiscounted,
            BigDecimal deliveryDistanceKm
    ) {

        log.info(
                "CALCULATE_CUSTOMER_DELIVERY_CHARGE | cityId={} | orderAmountDiscounted={} | deliveryDistanceKm={}",
                cityId,
                orderAmountDiscounted,
                deliveryDistanceKm
        );

        if (cityId == null) {
            throw new IllegalArgumentException("City id is required");
        }

        if (orderAmountDiscounted == null) {
            throw new IllegalArgumentException("Order amount discounted is required");
        }

        if (deliveryDistanceKm == null) {
            throw new IllegalArgumentException("Delivery distance is required");
        }

        if (orderAmountDiscounted.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Order amount discounted cannot be negative"
            );
        }

        if (deliveryDistanceKm.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Delivery distance cannot be negative"
            );
        }

        CustomerDeliveryChargeSettingsDTO plan =
                getApplicablePlan(
                        cityId,
                        orderAmountDiscounted
                );

        BigDecimal chargePerKm =
                plan.getChargePerKm();

        BigDecimal freeDistanceKms =
                plan.getFreeDistanceKms();

        /*
         * Gross customer delivery charge
         *
         * Example:
         * Distance = 6 KM
         * Charge/KM = ₹8
         *
         * Gross = 6 × 8 = ₹48
         */
        BigDecimal grossDeliveryCharge =
                deliveryDistanceKm
                        .multiply(chargePerKm)
                        .setScale(2, RoundingMode.HALF_UP);

        /*
         * Free-distance benefit
         *
         * Only the actual distance travelled can be free.
         *
         * Example:
         * Distance = 6 KM
         * Free KM = 3 KM
         *
         * Benefit = 3 × 8 = ₹24
         */
        BigDecimal applicableFreeDistance =
                deliveryDistanceKm.min(freeDistanceKms);

        BigDecimal freeDistanceBenefit =
                applicableFreeDistance
                        .multiply(chargePerKm)
                        .setScale(2, RoundingMode.HALF_UP);

        /*
         * Customer payable delivery charge
         */
        BigDecimal deliveryCharge =
                grossDeliveryCharge
                        .subtract(freeDistanceBenefit)
                        .max(BigDecimal.ZERO)
                        .setScale(2, RoundingMode.HALF_UP);

        CustomerDeliveryChargeCalculationResponseDto response =
                new CustomerDeliveryChargeCalculationResponseDto();

        response.setDeliveryDistanceKm(
                deliveryDistanceKm
        );

        response.setGrossDeliveryCharge(
                grossDeliveryCharge
        );

        response.setFreeDistanceKms(
                freeDistanceKms
        );

        response.setFreeDistanceBenefit(
                freeDistanceBenefit
        );

        response.setChargePerKm(
                chargePerKm
        );

        response.setDeliveryCharge(
                deliveryCharge
        );

        log.info(
                "CUSTOMER_DELIVERY_CHARGE_CALCULATED | cityId={} | plan={} | distance={} | chargePerKm={} | grossCharge={} | freeBenefit={} | payableCharge={}",
                cityId,
                plan.getPlanName(),
                deliveryDistanceKm,
                chargePerKm,
                grossDeliveryCharge,
                freeDistanceBenefit,
                deliveryCharge
        );

        return response;
    }

    private void validateDuplicateThreshold(Integer cityId, BigDecimal orderValueThreshold) {

        boolean exists = repository.existsByCityIdAndOrderValueThreshold(cityId, orderValueThreshold);

        if (exists) {
            throw new DuplicateResourceException("A delivery charge setting already exists for cityId " + cityId + " and order value threshold " + orderValueThreshold);
        }
    }

    private void mapDtoToEntity(CustomerDeliveryChargeSettingsDTO dto, CustomerDeliveryChargeSettings entity) {

        entity.setCityId(dto.getCityId());
        entity.setPlanName(dto.getPlanName());
        entity.setOrderValueThreshold(dto.getOrderValueThreshold());
        entity.setFreeDistanceKms(dto.getFreeDistanceKms());
        entity.setChargePerKm(dto.getChargePerKm());

        if (dto.getIsActive() != null) {
            entity.setIsActive(dto.getIsActive());
        }
    }

    private CustomerDeliveryChargeSettingsDTO mapEntityToDto(CustomerDeliveryChargeSettings entity) {

        CustomerDeliveryChargeSettingsDTO dto = new CustomerDeliveryChargeSettingsDTO();

        dto.setCustomerDeliveryChargeSettingsId(entity.getCustomerDeliveryChargeSettingsId());

        dto.setCityId(entity.getCityId());
        dto.setPlanName(entity.getPlanName());

        dto.setOrderValueThreshold(entity.getOrderValueThreshold());

        dto.setFreeDistanceKms(entity.getFreeDistanceKms());

        dto.setChargePerKm(entity.getChargePerKm());

        dto.setIsActive(entity.getIsActive());

        return dto;
    }
}