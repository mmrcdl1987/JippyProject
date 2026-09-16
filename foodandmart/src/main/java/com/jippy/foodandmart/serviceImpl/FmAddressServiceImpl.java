package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.repository.FmAddressRepository;
import com.jippy.foodandmart.service.FmAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FmAddressServiceImpl implements FmAddressService {

    private final FmAddressRepository fmAddressRepository;

    @Override
    public List<Integer> getDriverIdsByAreaId(Integer areaId) {

        return fmAddressRepository.findDriverIdsByAreaId(areaId);
    }
}