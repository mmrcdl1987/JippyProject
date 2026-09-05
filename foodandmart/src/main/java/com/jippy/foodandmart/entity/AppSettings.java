package com.jippy.foodandmart.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "app_settings", schema = "jippy_fm")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "app_settings_id")
    private Long appSettingsId;

    @Column(name = "app_name")
    private String appName;

    @Column(name = "package_name")
    private String packageName;

    @Column(name = "app_type")
    private String appType;

    @Column(name = "android_version")
    private String androidVersion;

    @Column(name = "android_build")
    private String androidBuild;

    @Column(name = "ios_version")
    private String iosVersion;

    @Column(name = "ios_build")
    private String iosBuild;

    @Column(name = "min_required_version")
    private String minRequiredVersion;

    @Column(name = "latest_version")
    private String latestVersion;

    @Column(name = "android_update_url")
    private String androidUpdateUrl;

    @Column(name = "ios_update_url")
    private String iosUpdateUrl;

    @Column(name = "force_update")
    private Boolean forceUpdate;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
}