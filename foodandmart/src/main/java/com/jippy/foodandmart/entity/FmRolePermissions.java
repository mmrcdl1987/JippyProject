package com.jippy.foodandmart.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name ="role_permissions",schema = "jippy_fm")
@Getter
@Setter
public class FmRolePermissions {

    @ManyToOne
    @JoinColumn(name = "role_id")
    private FmRoles role;

    @ManyToOne
    @JoinColumn(name = "permission_id")
    private FmPermission permission;

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column(name = "role_permission_id")
    private Integer rolePermissionId;

    private Integer createdBy;

    private LocalDateTime createdAt;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FmRolePermissions that = (FmRolePermissions) o;
        return Objects.equals(rolePermissionId, that.rolePermissionId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(rolePermissionId);
    }
}
