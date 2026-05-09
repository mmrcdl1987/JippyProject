package com.jippy.customerandorder.entity;


import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;




@Data
@Entity
@Table(name = "order_rejection", schema = "jippy_customer_and_order")
public class CoOrderRejection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderRejectionId;

    private String orderId;
    private Integer rejectedById;
    private String type; // CUSTOMER / OUTLET
    private String reason;

    private Boolean isActive;

    private LocalDateTime createdAt;
    private Integer createdBy;
    private LocalDateTime updatedAt;
    private Integer updatedBy;
}
