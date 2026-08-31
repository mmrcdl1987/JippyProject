package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.FmCreateEmployeeRequestDTO;
import com.jippy.foodandmart.dto.FmCreateEmployeeResponseDTO;
import com.jippy.foodandmart.dto.FmEmployeeSearchResponseDTO;

import java.util.List;

public interface IFmEmployeeService {

    /*
     * Creates a new employee along with login credentials and address details.
     */
    FmCreateEmployeeResponseDTO createEmployee(FmCreateEmployeeRequestDTO dto);

    /*
     * Searches employees by name or employee ID.
     */
    List<FmEmployeeSearchResponseDTO> searchEmployees(String query);
}
