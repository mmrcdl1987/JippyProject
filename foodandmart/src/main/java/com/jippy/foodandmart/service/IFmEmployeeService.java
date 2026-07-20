package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.FmCreateEmployeeRequestDTO;
import com.jippy.foodandmart.dto.FmCreateEmployeeResponseDTO;

public interface IFmEmployeeService {

    /*
     * Creates a new employee along with login credentials and address details.
     */
    FmCreateEmployeeResponseDTO createEmployee(FmCreateEmployeeRequestDTO dto);
}