package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.AppSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppSettingsRepository
        extends JpaRepository<AppSettings, Long> {

    Optional<AppSettings> findByAppTypeIgnoreCase(String appType);
}