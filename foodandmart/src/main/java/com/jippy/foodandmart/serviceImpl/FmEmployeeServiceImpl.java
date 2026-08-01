package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.dto.FmCreateEmployeeRequestDTO;
import com.jippy.foodandmart.dto.FmCreateEmployeeResponseDTO;
import com.jippy.foodandmart.entity.FmEmployee;
import com.jippy.foodandmart.entity.FmOutletAddress;
import com.jippy.foodandmart.entity.FmUser;
import com.jippy.foodandmart.exception.DuplicateResourceException;
import com.jippy.foodandmart.exception.ResourceNotFoundException;
import com.jippy.foodandmart.mapper.FmEmployeeMapper;
import com.jippy.foodandmart.repository.*;
import com.jippy.foodandmart.service.IFmEmployeeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FmEmployeeServiceImpl implements IFmEmployeeService {

    private final FmEmployeeRepository employeeRepository;

    private final FmUserRepository userRepository;

    private final FmOutletAddressRepository addressRepository;

    private final FmStateRepository stateRepository;

    private final FmCityRepository cityRepository;

    private final FmAreaRepository areaRepository;

    private final PasswordEncoder passwordEncoder;

    /**
     * Creates a new employee.
     * <p>
     * Saves:
     * 1. Employee Details
     * 2. Login Credentials
     * 3. Address Details
     */
    @Override
    @Transactional
    public FmCreateEmployeeResponseDTO createEmployee(FmCreateEmployeeRequestDTO dto) {

        log.info("Employee creation started for username : {}", dto.getUsername());

        /*
         * Validate duplicate employee email.
         */
        if (employeeRepository.existsByEmailIgnoreCase(dto.getEmail())) {

            log.error("Employee email already exists : {}", dto.getEmail());

            throw new DuplicateResourceException("Employee email already exists.");
        }

        /*
         * Validate duplicate mobile number.
         */
        if (employeeRepository.existsByMobileNumber(dto.getMobileNumber())) {

            log.error("Employee mobile number already exists : {}", dto.getMobileNumber());

            throw new DuplicateResourceException("Employee mobile number already exists.");
        }

        /*
         * Validate username.
         */
        if (userRepository.findByUsernameAndUserType(dto.getUsername(), FmAppConstants.TYPE_EMPLOYEE).isPresent()) {

            log.error("Username already exists : {}", dto.getUsername());

            throw new DuplicateResourceException("Username already exists.");
        }

        /*
         * Validate State.
         */
        stateRepository.findById(dto.getStateId()).orElseThrow(() -> {

            log.error("State not found : {}", dto.getStateId());

            return new ResourceNotFoundException("State not found with id : " + dto.getStateId());
        });

        /*
         * Validate City.
         */
        cityRepository.findById(dto.getCityId()).orElseThrow(() -> {

            log.error("City not found : {}", dto.getCityId());

            return new ResourceNotFoundException("City not found with id : " + dto.getCityId());
        });

        /*
         * Validate Area.
         */
        areaRepository.findById(dto.getAreaId()).orElseThrow(() -> {

            log.error("Area not found : {}", dto.getAreaId());

            return new ResourceNotFoundException("Area not found with id : " + dto.getAreaId());
        });

        /*
         * Convert Request DTO to Employee Entity.
         */
        FmEmployee employee = FmEmployeeMapper.toEmployeeEntity(dto);

        /*
         * Save Employee.
         */
        employee = employeeRepository.save(employee);

        log.info("Employee saved successfully with employeeId : {}", employee.getEmployeeId());

        /*
         * Save Employee Login Details.
         */
        saveEmployeeUser(dto, employee.getEmployeeId());

        log.info("Employee user created successfully for employeeId : {}", employee.getEmployeeId());

        /*
         * Save Employee Address.
         */
        saveEmployeeAddress(dto, employee.getEmployeeId());

        log.info("Employee address created successfully for employeeId : {}", employee.getEmployeeId());

        /*
         * Prepare response.
         */
        FmCreateEmployeeResponseDTO response = FmEmployeeMapper.toCreateEmployeeResponseDto(dto, employee);

        log.info("Employee created successfully with employeeId : {}", employee.getEmployeeId());

        return response;
    }

//    HELPER METHODS for Creating EMPLOYEE
    /**
     * Saves employee login credentials into users table.
     * <p>
     * Every employee must have a login account with
     * user_type = EMPLOYEE.
     */
    private void saveEmployeeUser(FmCreateEmployeeRequestDTO dto, Integer employeeId) {

        log.info("Saving employee user for employeeId : {}", employeeId);

        /*
         * Validate username.
         */
        if (userRepository.findByUsernameAndUserType(dto.getUsername(),
                FmAppConstants.TYPE_EMPLOYEE).isPresent()) {

            throw new DuplicateResourceException("Username already exists.");
        }

        /*
         * Convert request DTO to User entity.
         */
        FmUser user = FmEmployeeMapper.toEmployeeUserEntity(dto, employeeId);

        /*
         * Encrypt password before storing.
         */
        String encodedPassword = passwordEncoder.encode(dto.getPassword());

        user.setPassword(encodedPassword);

        /*
         * Save employee user.
         */
        userRepository.save(user);

        log.info("Employee user saved successfully.");
    }

    /**
     * Saves employee address into address table.
     * <p>
     * Every employee can have one address record with
     * address_type = EMPLOYEE.
     */
    private void saveEmployeeAddress(FmCreateEmployeeRequestDTO dto, Integer employeeId) {

        log.info("Saving employee address for employeeId : {}", employeeId);

        /*
         * Check whether address already exists.
         */
        Optional<FmOutletAddress> address =
                addressRepository.findByJippyAddressIdAndAddressType
                        (employeeId, FmAppConstants.TYPE_EMPLOYEE);

        if (address.isPresent()) {

            log.error("Employee address already exists for employeeId : {}", employeeId);

            throw new DuplicateResourceException("Employee address already exists.");
        }

        /*
         * Convert Request DTO to Address Entity.
         */
        FmOutletAddress employeeAddress = FmEmployeeMapper.toEmployeeAddressEntity(dto, employeeId);

        /*
         * Save employee address.
         */
        addressRepository.save(employeeAddress);

        log.info("Employee address saved successfully.");
    }
}