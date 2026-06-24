package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmEmployee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FmEmployeeRepository extends JpaRepository<FmEmployee, Integer> {

    // for finding the email in te Employees table
    Optional<FmEmployee> findByEmailIgnoreCase(String employeeEmail);
}
