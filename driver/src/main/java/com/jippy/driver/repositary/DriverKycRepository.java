package com.jippy.driver.repositary;


import com.jippy.driver.entity.DriverKyc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DriverKycRepository extends JpaRepository<DriverKyc, Integer> {

    // Check whether Aadhaar already exists
    boolean existsByAadharNumber(String aadharNumber);

    // Check whether PAN already exists
    boolean existsByPanNumber(String panNumber);

    // Check whether Driving License already exists
    boolean existsByDrivingLicenseNumber(String drivingLicenseNumber);

    // Check whether RC Copy already exists
    boolean existsByRcCopy(String rcCopy);

    // Find DriverKyc by driver ID
    Optional<DriverKyc> findByDriverDriverId(Integer driverId);

}
