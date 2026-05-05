package com.jippy.customerandorder.iservice;


import com.jippy.customerandorder.dto.CoDriverDto;

public interface ICoDriverService {

    CoDriverDto postDriverDetails(CoDriverDto dto);

    CoDriverDto getDriverDetails(Integer driverId);
}