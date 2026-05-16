package com.jippy.foodandmart.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;


@Entity
@Setter
@Getter
@Table (name ="user_role_permissions",schema = "jippy_fm")
 public class FmUserRolePermissions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_role_permission_id")
    private Long userRolePermissionId;

    @ManyToOne
    @JoinColumn(name = "role_permission_id")
    private FmRolePermissions rolePermission;

    @Column(name = "user_id")
    private Integer userId;

    private Integer createdBy;

    private LocalDateTime createdAt;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FmUserRolePermissions that = (FmUserRolePermissions) o;
        return Objects.equals(userRolePermissionId, that.userRolePermissionId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userRolePermissionId);
    }
}
