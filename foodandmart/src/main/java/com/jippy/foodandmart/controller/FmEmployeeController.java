package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.FmApiResponse;
import com.jippy.foodandmart.dto.FmCreateEmployeeRequestDTO;
import com.jippy.foodandmart.dto.FmCreateEmployeeResponseDTO;
import com.jippy.foodandmart.service.IFmEmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fm/employees")
@RequiredArgsConstructor
@Slf4j
public class FmEmployeeController {

    private final IFmEmployeeService employeeService;

    @PostMapping("/createEmployee")
    @Operation(summary = "Create Employee", description = """
            Creates a new employee by saving:
          
            • Employee Details
            • Employee Login Credentials
            • Employee Address
            
            Username, Email and Mobile Number must be unique.
            """)
    @ApiResponses(value = {

            @ApiResponse(responseCode = "200", description = "Employee created successfully"),

            @ApiResponse(responseCode = "400", description = "Invalid request"),

            @ApiResponse(responseCode = "404", description = "State, City or Area not found"),

            @ApiResponse(responseCode = "409", description = "Employee already exists")})
    public ResponseEntity<FmApiResponse<FmCreateEmployeeResponseDTO>> createEmployee(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Employee creation request", required = true,
                    content = @Content(
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "employeeName":"Rohan",
                                              "email":"rohan@gmail.com",
                                              "mobileNumber":"9876543210",
                                            
                                              "username":"rohan_emp",
                                              "password":"Rohan@123",
                                            
                                              "buildingNumber":"10-1-20",
                                              "road":"Main Road",
                                              "landmark":"Near Metro",
                                            
                                              "stateId":2,
                                              "cityId":3,
                                              "areaId":15,
                                            
                                              "createdBy":101
                                            }
                                            """)))
            @Valid  @RequestBody FmCreateEmployeeRequestDTO dto) {

        log.info("Received request to create employee with username : {}", dto.getUsername());

        FmCreateEmployeeResponseDTO response = employeeService.createEmployee(dto);

        log.info("Employee created successfully with username : {}", dto.getUsername());

        return ResponseEntity.ok(FmApiResponse.success("Employee created successfully",
                response));
    }
}