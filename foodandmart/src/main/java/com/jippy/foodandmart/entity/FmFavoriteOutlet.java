package com.jippy.foodandmart.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "favorite_outlets", schema = "jippy_fm")
@Getter
@Setter
public class FmFavoriteOutlet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer favoriteOutletsId;

    @Column(name = "customer_id")
    private Integer customerId;

//    @Column(name = "outlet_id")
//    private Integer outletId;

    @Column(name = "favorite_id")
    private Integer favoriteId;

    @Column(name = "favourite_type")
    private String favouriteType;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}