package com.jippy.customerandorder.serviceImpl;

import com.jippy.customerandorder.dto.CoOrderCheckoutTaxRequestDto;
import com.jippy.customerandorder.dto.CoOrderCheckoutTaxResponseDto;
import com.jippy.customerandorder.entity.CoOrderCheckoutTax;
import com.jippy.customerandorder.exception.CoResourceNotFoundException;
import com.jippy.customerandorder.iservice.CoOrderCheckoutTaxService;
import com.jippy.customerandorder.mapper.CoOrderCheckoutTaxMapper;
import com.jippy.customerandorder.repository.CoOrderCheckoutTaxRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoOrderCheckoutTaxServiceImpl implements CoOrderCheckoutTaxService {

    private final CoOrderCheckoutTaxRepository repository;
    private final CoOrderCheckoutTaxMapper mapper;

    @Override
    @Transactional
    public CoOrderCheckoutTaxResponseDto create(CoOrderCheckoutTaxRequestDto request) {

        log.info("CREATE_ORDER_CHECKOUT_TAX");

        CoOrderCheckoutTax entity = mapper.toEntity(request);

        entity.setCreatedBy(request.getUserId());

        CoOrderCheckoutTax savedEntity = repository.save(entity);

        return mapper.toResponse(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public CoOrderCheckoutTaxResponseDto getById(Integer orderCheckoutTaxId) {

        log.info("GET_ORDER_CHECKOUT_TAX | id={}", orderCheckoutTaxId);

        CoOrderCheckoutTax entity = findById(orderCheckoutTaxId);

        return mapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoOrderCheckoutTaxResponseDto> getAll() {

        log.info("GET_ALL_ORDER_CHECKOUT_TAX");

        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional
    public CoOrderCheckoutTaxResponseDto update(Integer orderCheckoutTaxId, CoOrderCheckoutTaxRequestDto request) {

        log.info("UPDATE_ORDER_CHECKOUT_TAX | id={}", orderCheckoutTaxId);

        CoOrderCheckoutTax entity = findById(orderCheckoutTaxId);

        mapper.updateEntity(entity, request);

        entity.setUpdatedBy(request.getUserId());

        CoOrderCheckoutTax updatedEntity = repository.save(entity);

        return mapper.toResponse(updatedEntity);
    }

    @Override
    @Transactional
    public void delete(Integer orderCheckoutTaxId) {

        log.info("DELETE_ORDER_CHECKOUT_TAX | id={}", orderCheckoutTaxId);

        CoOrderCheckoutTax entity = findById(orderCheckoutTaxId);

        repository.delete(entity);
    }

    private CoOrderCheckoutTax findById(Integer orderCheckoutTaxId) {

        return repository.findById(orderCheckoutTaxId).orElseThrow(() -> new CoResourceNotFoundException("Order checkout tax not found for id : " + orderCheckoutTaxId));
    }
}