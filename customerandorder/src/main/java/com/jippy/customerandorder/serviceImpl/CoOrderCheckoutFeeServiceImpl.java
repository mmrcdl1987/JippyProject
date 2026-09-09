package com.jippy.customerandorder.serviceImpl;

import com.jippy.customerandorder.dto.CoOrderCheckoutFeeRequestDto;
import com.jippy.customerandorder.dto.CoOrderCheckoutFeeResponseDto;
import com.jippy.customerandorder.entity.CoOrderCheckoutFee;
import com.jippy.customerandorder.exception.CoBusinessException;
import com.jippy.customerandorder.exception.CoResourceNotFoundException;
import com.jippy.customerandorder.iservice.CoOrderCheckoutFeeService;
import com.jippy.customerandorder.mapper.CoOrderCheckoutFeeMapper;
import com.jippy.customerandorder.repository.CoOrderCheckoutFeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoOrderCheckoutFeeServiceImpl implements CoOrderCheckoutFeeService {

    private final CoOrderCheckoutFeeRepository repository;

    private final CoOrderCheckoutFeeMapper mapper;

    @Override
    @Transactional
    public CoOrderCheckoutFeeResponseDto create(CoOrderCheckoutFeeRequestDto request) {

        log.info("SERVICE_START | CREATE_ORDER_CHECKOUT_FEE | areaId={}", request.getAreaId());

        if (repository.findByAreaId(request.getAreaId()).isPresent()) {

            log.warn(
                    "ORDER_CHECKOUT_FEE_ALREADY_EXISTS | areaId={}",
                    request.getAreaId()
            );

            throw new CoBusinessException(
                    "Order checkout fee configuration already exists for area id : "
                            + request.getAreaId()
            );
        }

        CoOrderCheckoutFee entity = mapper.toEntity(request);

        entity.setCreatedBy(1);

        CoOrderCheckoutFee savedEntity = repository.save(entity);

        log.info("SERVICE_SUCCESS | CREATE_ORDER_CHECKOUT_FEE | id={} | areaId={}", savedEntity.getOrderCheckoutFeeId(), savedEntity.getAreaId());

        return mapper.toResponse(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public CoOrderCheckoutFeeResponseDto getById(Integer orderCheckoutFeeId) {

        log.info("SERVICE_START | GET_ORDER_CHECKOUT_FEE | id={}", orderCheckoutFeeId);

        CoOrderCheckoutFee entity = findById(orderCheckoutFeeId);

        return mapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoOrderCheckoutFeeResponseDto> getAll() {

        log.info("SERVICE_START | GET_ALL_ORDER_CHECKOUT_FEE");

        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional
    public CoOrderCheckoutFeeResponseDto update(Integer orderCheckoutFeeId, CoOrderCheckoutFeeRequestDto request) {

        log.info("SERVICE_START | UPDATE_ORDER_CHECKOUT_FEE | id={} | areaId={}", orderCheckoutFeeId, request.getAreaId());

        CoOrderCheckoutFee entity = findById(orderCheckoutFeeId);

        Optional<CoOrderCheckoutFee> existingAreaConfiguration = repository.findByAreaIdAndOrderCheckoutFeeIdNot(request.getAreaId(), orderCheckoutFeeId);

        if (existingAreaConfiguration.isPresent()) {

            log.warn("ORDER_CHECKOUT_FEE_AREA_ALREADY_EXISTS | id={} | areaId={} | existingId={}", orderCheckoutFeeId, request.getAreaId(), existingAreaConfiguration.get().getOrderCheckoutFeeId());

            throw new CoBusinessException("Order checkout fee configuration already exists for area id : " + request.getAreaId());
        }

        mapper.updateEntity(entity, request);

        entity.setUpdatedBy(request.getUserId());

        CoOrderCheckoutFee updatedEntity = repository.save(entity);

        log.info("SERVICE_SUCCESS | UPDATE_ORDER_CHECKOUT_FEE | id={} | areaId={}", orderCheckoutFeeId, updatedEntity.getAreaId());

        return mapper.toResponse(updatedEntity);
    }

    @Override
    @Transactional
    public void delete(Integer orderCheckoutFeeId) {

        log.info("SERVICE_START | DELETE_ORDER_CHECKOUT_FEE | id={}", orderCheckoutFeeId);

        CoOrderCheckoutFee entity = findById(orderCheckoutFeeId);

        repository.delete(entity);

        log.info("SERVICE_SUCCESS | DELETE_ORDER_CHECKOUT_FEE | id={}", orderCheckoutFeeId);
    }

    private CoOrderCheckoutFee findById(Integer orderCheckoutFeeId) {

        return repository.findById(orderCheckoutFeeId).orElseThrow(() -> new CoResourceNotFoundException("Order checkout fee not found for id : " + orderCheckoutFeeId));
    }
}