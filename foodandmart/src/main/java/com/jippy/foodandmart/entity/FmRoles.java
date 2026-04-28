
package com.jippy.foodandmart.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
@Data
@Entity
@Table(name = "roles")
public class FmRoles {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)

    @Column(name = "role_id")
    private Integer roleId;
    @Column(name ="role_name")
    private String roleName;
    @ManyToMany
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<FmPermission> permissions = new HashSet<>();

    private Integer createdBy;
    private LocalDateTime createdAt;
    private Integer updatedBy;
    private LocalDateTime updatedAt;

//    @ManyToOne
//    @JoinColumn(name = "user_id")
//    private User user;
}
