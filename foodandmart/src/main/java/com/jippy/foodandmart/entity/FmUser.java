package com.jippy.foodandmart.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users", schema = "jippy_fm")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FmUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "users_id")
    private Integer userId;

    @Column(name = "username", length = 100, unique = true)
    private String username;
    @Column(name = "password", length = 100)
    private String password;
    @Column(name = "user_id", nullable = false)
    private Integer employeeId;
    @Column(name = "user_type", length = 50)
    private String userType;
    @Column(name = "is_active", length = 1)
    private String isActive;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "created_by")
    private Integer createdBy;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "updated_by")
    private Integer updatedBy;

    @ManyToMany
    @JoinTable(
            name = "user_role_permissions",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_permission_id")
    )
//    @Builder.Default
//    private Set<Roles> roles = new HashSet<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.isActive == null) this.isActive = "N";
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
