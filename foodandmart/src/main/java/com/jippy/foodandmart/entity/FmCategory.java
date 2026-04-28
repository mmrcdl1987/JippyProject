package com.jippy.foodandmart.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories", schema = "jippy_fm")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FmCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Integer categoryId;

    @Column(name = "category_name", length = 100, nullable = false)
    private String categoryName;

    @Column(name = "created_at") private LocalDateTime createdAt;
    @Column(name = "created_by") private Integer createdBy;
    @Column(name = "updated_at") private LocalDateTime updatedAt;
    @Column(name = "updated_by") private Integer updatedBy;

    @JsonIgnore
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<FmOutletCategory> outletCategories = new ArrayList<>();

    @PrePersist public void prePersist() { this.createdAt = LocalDateTime.now(); }
    @PreUpdate  public void preUpdate()  { this.updatedAt = LocalDateTime.now(); }
}
