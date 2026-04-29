package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmOutletDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FmOutletDayRepository extends JpaRepository<FmOutletDay, Integer> {
    List<FmOutletDay> findByOutletId(Integer outletId);
}
