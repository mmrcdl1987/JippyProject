package com.jippy.division.entity;

import com.jippy.division.enums.LocationType;
import com.jippy.division.enums.PromotionScheduleStatus;
import com.jippy.division.enums.PromotionSourceType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "promotion_schedules", schema = "jippy_division")
public class PromotionSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "promotion_schedule_id")
    private Long promotionScheduleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type")
    private PromotionSourceType sourceType;

    @Column(name = "source_id", nullable = false)
    private Integer sourceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "location_type")
    private LocationType locationType;

    @Column(name = "location_id", nullable = false)
    private Integer locationId;

    @Column(name = "outlet_id", nullable = false)
    private Integer outletId;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Column(name = "start_date_time", nullable = false)
    private LocalDateTime startDateTime;

    @Column(name = "end_date_time", nullable = false)
    private LocalDateTime endDateTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PromotionScheduleStatus status;
}