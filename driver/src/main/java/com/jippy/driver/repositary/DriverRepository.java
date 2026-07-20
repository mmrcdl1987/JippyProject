package com.jippy.driver.repositary;



import com.jippy.driver.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Integer> {
    Optional<Driver> findByEmailIgnoreCase(String email);

    Optional<Driver> findByDriverId(Integer driverId);
}