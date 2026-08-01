package com.jippy.customerandorder.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoCartReminderDto {

    private Integer customerId;

    private BigDecimal cartTotal;

    private LocalDateTime lastUpdated;

    private String notificationSubject;

}
