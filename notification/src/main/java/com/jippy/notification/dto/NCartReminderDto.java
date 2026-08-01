package com.jippy.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NCartReminderDto {

    private Integer customerId;

    private BigDecimal cartTotal;

    private LocalDateTime lastUpdated;

    private String notificationSubject;
}