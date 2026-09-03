package com.jippy.customerandorder.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders", schema = "jippy_customer_and_order")
@Getter
@Setter
public class CoOrder {

    @Id
    @Column(name = "order_id")
    private String orderId;

    @Column(name = "customer_id", nullable = false)
    private Integer customerId;

    @Column(name = "outlet_id", nullable = false)
    private Integer outletId;

    @Column(name = "driver_id")
    private Integer driverId;
    /* * PLACED * DELIVERED * CANCELLED */
    @Column(name = "order_status", nullable = false)
    private String orderStatus;

    @Column(name = "customer_delivery_address_id", nullable = false)
    private Integer customerDeliveryAddressId;

    @Column(name = "customer_phone_number", nullable = false)
    private String customerPhoneNumber;

    @Column(name = "preparation_time_in_mins")
    private Integer preparationTime;

    @Column(name = "estimated_delivery_time")
    private LocalDateTime estimatedDeliveryTime;

    @Column(name = "distance_kms")
    private Double distanceKms;

//  added column for COD in payment_node table
    @Column(name = "payment_mode_id")
    private Integer paymentModeId;

    /* * NORMAL * SCHEDULED */
    @Column(name = "order_type")
    private String orderType;

    /* * Delivery date & time */
    @Column(name = "scheduled_delivery_date_time")
    private LocalDateTime scheduledDeliveryDateTime;

    /*
     * Linked subscription id
     */
    @Column(name = "meal_subscription_id")
    private Integer mealSubscriptionId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "group_order_invitation_id")
    private Integer groupOrderInvitationId;

    @Column(name = "cooking_instructions")
    private String cookingInstructions;

    @Column(name = "is_cutlery_required")
    private Boolean isCutleryRequired;

    // One Order has Many Order Items
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CoOrderItem> orderItems = new ArrayList<>();

    // For normal orders, this will have only one entry. For group orders/community orders, this will have multiple entries for each participant.
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CoOrderPriceBreakup> priceBreakups = new ArrayList<>();

}