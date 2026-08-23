package com.jippy.customerandorder.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "wallet_settings", schema = "jippy_customer_and_order")
public class CoWalletSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wallet_settings_id")
    private Integer walletSettingsId;

    @Column(name = "setting_type")
    private String settingType;

    @Column(name = "setting_value")
    private Integer settingValue;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;
}