package com.jippy.customerandorder.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "customer_communities", schema = "jippy_customer_and_order")
@Data
public class CoCustomerCommunities {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer customerCommunitiesId;
    private Integer customerId;
    private Integer communityId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer createdBy;
    private Integer updatedBy;


}
