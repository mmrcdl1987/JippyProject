package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.CoOrderSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CoOrderSettingsRepository extends JpaRepository<CoOrderSettings, Integer> {

    Optional<CoOrderSettings> findTopByOrderByOrderSettingsIdDesc();
}
