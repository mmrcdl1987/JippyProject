package com.jippy.driver.repositary;


import com.jippy.driver.entity.DriverKyc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DriverKycRepository extends JpaRepository<DriverKyc, Integer> {
}