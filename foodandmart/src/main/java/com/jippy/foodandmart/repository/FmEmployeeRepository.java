package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmEmployee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FmEmployeeRepository extends JpaRepository<FmEmployee, Integer> {
}
