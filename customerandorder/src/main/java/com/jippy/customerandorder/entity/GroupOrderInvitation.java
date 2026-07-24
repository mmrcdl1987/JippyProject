package com.jippy.customerandorder.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "group_orders_invitation",schema = "jippy_customer_and_order")
@Data
public class GroupOrderInvitation {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Integer groupOrdersInvitationId;

    private Integer hostCustomerId;
    private Integer outletId;
    private String invitationCode;
    private String status;

    private Integer orderClosingTimeInMinutes;

    private String paymentResponsibility;

    private Integer maxMembers;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    private Integer createdBy;

    private Integer updatedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "groupOrdersInvitation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GroupOrderMembers> groupOrderMembers;

    @OneToMany(mappedBy = "groupOrders", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GroupCartItems> cartItems;

    private String orderType;

    private Integer communityEventId;

    private Integer communityId;

    @Override
    public String toString() {
        return "GroupOrderInvitation{" +
                "groupOrdersInvitationId=" + groupOrdersInvitationId +
                ", hostCustomerId=" + hostCustomerId +
                ", outletId=" + outletId +
                ", invitationCode='" + invitationCode + '\'' +
                ", status='" + status + '\'' +
                ", orderClosingTimeInMinutes=" + orderClosingTimeInMinutes +
                ", paymentResponsibility='" + paymentResponsibility + '\'' +
                ", maxMembers=" + maxMembers +
                ", createdAt=" + createdAt +
                ", createdBy=" + createdBy +
                ", updatedBy=" + updatedBy +
                ", updatedAt=" + updatedAt +
                ", groupOrderMembers=" + groupOrderMembers +
                ", cartItems=" + cartItems +
                '}';
    }
}
