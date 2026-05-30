package com.jippy.customerandorder.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "meal_subscription", schema = "jippy_customer_and_order")
@Getter
@Setter
public class CoMealSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meal_subscription_id")
    private Integer mealSubscriptionId;

    @Column(name = "customer_id")
    private Integer customerId;

    @Column(name = "outlet_id")
    private Integer outletId;

    /* * BREAKFAST * LUNCH * DINNER */
    @Column(name = "meal_preference")
    private String mealPreference;
    /*
     * Subscription starts from
     */
    @Column(name = "subscription_start_date")
    private LocalDateTime subscriptionStartDate;

    /*
     * Subscription ends on
     */
    @Column(name = "subscription_end_date")
    private LocalDateTime subscriptionEndDate;

    /* * ACTIVE * PAUSED * CANCELLED * COMPLETED */
    @Column(name = "subscription_status")
    private String subscriptionStatus;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}