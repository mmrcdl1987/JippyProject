package com.jippy.foodandmart.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table (name ="user_role_permissions")
 public class FmUserRolePermissions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userRolePermissionId;


    @JoinColumn(name = "role_permission_id")
    @Column (name="role_permission_id")
    private Integer rolePermissionId;

    @JoinColumn(name = "user_id")
    @Column(name="user_id")
    private Integer userId;

    private Integer createdBy;

    private LocalDateTime createdAt;
}
