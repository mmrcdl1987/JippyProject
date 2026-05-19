package com.jippy.driver.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "driver_wallet", schema = "jippy_driver")
@Data
public class DriverWallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer driverWalletId;

//    from drivers table
    private Integer driverId;

//    by default 1000
    private BigDecimal totalCodAmount;

//    by default false
    private Boolean ordersLock;

    private LocalDateTime createdAt;
    private Integer createdBy;

    private LocalDateTime updatedAt;
    private Integer updatedBy;
}