package com.jippy.foodandmart.mapper;

import com.jippy.foodandmart.dto.FmOutletDayDTO;
import com.jippy.foodandmart.entity.FmOutletDay;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class FmOutletDayMapper {

    // Prevent instantiation since this is a utility class
    private FmOutletDayMapper() {}

    // Formatter to parse and format time in "HH:mm" format
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public static FmOutletDay toEntity(FmOutletDayDTO dto, Integer outletId, Integer createdBy) {
        // Create new entity object
        FmOutletDay entity = new FmOutletDay();

        // Set outlet ID (foreign key)
        entity.setOutletId(outletId);

        // Set day of week (e.g., Monday, Tuesday)
        entity.setDayOfWeekId(dto.getDayOfWeekId());

        // If isOpen is null, default it to true
        entity.setIsOpen(dto.getIsOpen() != null ? dto.getIsOpen() : true);

        // Convert opening time string → LocalTime
        entity.setOpeningTime(parseTime(dto.getOpeningTime()));

        // Convert closing time string → LocalTime
        entity.setClosingTime(parseTime(dto.getClosingTime()));

        // Set current timestamp as created time
        entity.setCreatedAt(LocalDateTime.now());

        // Set user who created this record
        entity.setCreatedBy(createdBy);

        // Return populated entity
        return entity;
    }

    public static FmOutletDayDTO toDTO(FmOutletDay entity) {
        // Create new DTO object
        FmOutletDayDTO dto = new FmOutletDayDTO();

        // Set day of week
        dto.setDayOfWeekId(entity.getDayOfWeekId());

        // Set open/close status
        dto.setIsOpen(entity.getIsOpen());

        // Convert opening time → "HH:mm" string (null-safe)
        dto.setOpeningTime(entity.getOpeningTime() != null ? entity.getOpeningTime().format(TIME_FMT) : null);

        // Convert closing time → "HH:mm" string (null-safe)
        dto.setClosingTime(entity.getClosingTime() != null ? entity.getClosingTime().format(TIME_FMT) : null);

        // Return DTO
        return dto;
    }

    private static LocalTime parseTime(String timeStr) {
        // If input is null or empty, return default time 00:00
        if (timeStr == null || timeStr.isBlank()) return LocalTime.of(0, 0);

        // Parse string to LocalTime using formatter
        return LocalTime.parse(timeStr.trim(), TIME_FMT);
    }
}
