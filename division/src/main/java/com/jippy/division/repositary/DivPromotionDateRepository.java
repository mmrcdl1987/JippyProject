package com.jippy.division.repositary;

import com.jippy.division.entity.DivPromotionDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DivPromotionDateRepository
        extends JpaRepository<DivPromotionDate, Integer> {
}