package com.jippy.driver.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "driver_incentive_history", schema = "jippy_driver")
@Data
public class DriverIncentiveHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "driver_incentive_history_id")
    private Integer driverIncentiveHistoryId;

    @Column(name = "driver_id")
    private Integer driverId;

    @Column(name = "curr_date")
    private LocalDate currDate;

    @Column(name = "incentive_amount")
    private BigDecimal incentiveAmount;

    @Column(name = "completed_orders_count")
    private Integer completedOrdersCount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;
}