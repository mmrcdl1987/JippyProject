package com.jippy.division.repositary;

import com.jippy.division.entity.DivPromotionTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DivPromotionTimeRepository
        extends JpaRepository<DivPromotionTime, Integer> {
}