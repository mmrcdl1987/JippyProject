package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmOutletDay;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FmOutletDayRepository extends JpaRepository<FmOutletDay, Integer> {
    List<FmOutletDay> findByOutletId(Integer outletId);
    @Query("SELECT od FROM FmOutletDay od WHERE od.outletId IN :outletIds")
    List<FmOutletDay> findByOutletIdIn(@Param("outletIds") List<Integer> outletIds);

    @Transactional
    @Modifying
    @Query("DELETE FROM FmOutletDay od WHERE od.outletId = :outletId")
    void deleteByOutletId(@Param("outletId") Integer outletId);

}
