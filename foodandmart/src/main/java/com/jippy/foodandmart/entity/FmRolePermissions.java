package com.jippy.foodandmart.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name ="role_permissions")
@Data
public class FmRolePermissions {


    @JoinColumn(name = "role_id")
    @Column(name = "role_id")
    private Integer roleId;
    @Column(name = "permission_id")
    @JoinColumn(name = "permission_id")
    private  Integer permissionId;

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column(name = "role_permission_id")
    private Integer rolePermissionId;

    private Integer createdBy;

    private LocalDateTime createdAt;
}
