package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.CoOrderWaitingPeriod;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CoOrderWaitingPeriodRepository
        extends JpaRepository<
        CoOrderWaitingPeriod,
        Integer> {

    Optional<CoOrderWaitingPeriod>
    findByOrderRejectionId(Integer orderRejectionId);

    @Query("""
    SELECT w
    FROM CoOrderWaitingPeriod w
    WHERE w.allowsRejection = false
    AND w.createdAt <= :time
    """)
    List<CoOrderWaitingPeriod>
    fetchPendingWaitingPeriods(
            LocalDateTime time);
}