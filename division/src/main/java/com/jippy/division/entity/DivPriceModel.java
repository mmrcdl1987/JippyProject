package com.jippy.division.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "price_model", schema = "jippy_division")
@Data
public class DivPriceModel {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Integer priceModelId;
    private String priceModelName;

    private Integer createdBy;
    private LocalDateTime createdAt;
    private Integer updatedBy;
    private  LocalDateTime updatedAt;
}
