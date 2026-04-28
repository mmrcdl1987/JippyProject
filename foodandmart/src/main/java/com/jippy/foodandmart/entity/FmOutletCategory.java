package com.jippy.foodandmart.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "outlet_categories", schema = "jippy_fm")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FmOutletCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "outlet_category_id")
    private Integer outletCategoryId;

    @Column(name = "outlet_id",   nullable = false) private Integer outletId;
    @Column(name = "category_id", nullable = false) private Integer categoryId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outlet_id",   nullable = false, insertable = false, updatable = false)
    private FmOutlet outlet;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false, insertable = false, updatable = false)
    private FmCategory category;

    @Column(name = "created_at") private LocalDateTime createdAt;
    @Column(name = "created_by") private Integer createdBy;
    @Column(name = "updated_at") private LocalDateTime updatedAt;
    @Column(name = "updated_by") private Integer updatedBy;

    @PrePersist public void prePersist() { this.createdAt = LocalDateTime.now(); }
    @PreUpdate  public void preUpdate()  { this.updatedAt = LocalDateTime.now(); }
}
