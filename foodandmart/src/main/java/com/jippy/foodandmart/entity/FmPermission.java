package com.jippy.foodandmart.entity;



import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "permissions")
public class FmPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name ="permission_id")
    private Integer permissionId;
    private String permissionName;
    private Integer createdBy;
    private LocalDateTime createdAt;
    private Integer updatedBy;
    private LocalDateTime updatedAt;



    @ManyToMany(mappedBy = "permissions")
    private Set<FmRoles> roles = new HashSet<>();

}

