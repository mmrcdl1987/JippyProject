

package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.CoWalletSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CoWalletSettingsRepository extends JpaRepository<CoWalletSettings, Integer> {

    Optional<CoWalletSettings> findByPointsType(String pointsType);
}