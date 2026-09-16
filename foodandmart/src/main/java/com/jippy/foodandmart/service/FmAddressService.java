package com.jippy.foodandmart.service;

import java.util.List;

public interface FmAddressService {
    List<Integer> getDriverIdsByAreaId(Integer areaId);
}
