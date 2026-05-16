
package com.jippy.foodandmart.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
@Getter
@Setter
@Entity
@Table(name = "roles", schema = "jippy_fm")
public class FmRoles {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Integer roleId;
    @Column(name ="role_name")
    private String roleName;
  /*  @ManyToMany
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<FmPermission> permissions = new HashSet<>();*/

    @ManyToMany
    @JoinTable(
            name = "role_permissions",
            schema = "jippy_fm",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<FmPermission> permissions = new HashSet<>();

    @OneToMany(mappedBy = "role")
    private Set<FmRolePermissions> rolePermissions = new HashSet<>();

    private Integer createdBy;
    private LocalDateTime createdAt;
    private Integer updatedBy;
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FmRoles fmRoles = (FmRoles) o;
        return Objects.equals(roleId, fmRoles.roleId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(roleId);
    }
}
