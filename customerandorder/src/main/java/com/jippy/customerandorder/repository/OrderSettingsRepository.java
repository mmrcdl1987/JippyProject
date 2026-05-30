package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.CoOrderSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderSettingsRepository extends JpaRepository<CoOrderSettings, Integer> {
}