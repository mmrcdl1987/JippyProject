package com.jippy.division.repositary;

import com.jippy.division.entity.DivPromotionDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DivPromotionDateRepository
        extends JpaRepository<DivPromotionDate, Integer> {

    List<DivPromotionDate> findByCreatedAt(LocalDateTime createdAt);

    void deleteAllByCreatedAt(LocalDateTime createdAt);
}