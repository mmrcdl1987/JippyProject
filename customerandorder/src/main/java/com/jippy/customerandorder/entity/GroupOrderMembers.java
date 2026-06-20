package com.jippy.customerandorder.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_order_members",schema = "jippy_customer_and_order")
@Data
public class GroupOrderMembers {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Integer groupOrderMembersId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_orders_invitation_id", nullable = false)
    private GroupOrderInvitation groupOrdersInvitation;

    // Many member slots can point to different unique Customers
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", referencedColumnName = "customer_id", nullable = false)
    private CoCustomer customer;

    private Integer deliveryAddressId;
    private Boolean orderPlaced;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    private Integer createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    private Integer updatedBy;

    private boolean isDropped;
}
