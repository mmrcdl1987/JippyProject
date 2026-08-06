package com.jippy.division.repositary;

import com.jippy.division.entity.PromotionSchedule;
import com.jippy.division.enums.PromotionSourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PromotionScheduleRepository
        extends JpaRepository<PromotionSchedule, Long> {

    void deleteBySourceTypeAndSourceId(
            PromotionSourceType sourceType,
            Integer sourceId);
}