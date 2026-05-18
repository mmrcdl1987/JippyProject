package com.jippy.foodandmart.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "users", schema = "jippy_fm")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FmUser implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "users_id")
    private Integer usersId;

    @Column(name = "username", length = 100, unique = true)
    private String username;
    @Column(name = "password", length = 100)
    private String password;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

//    @Column(name = "user_id", nullable = false)
//    private Integer employeeId;
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

   /* @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_role_permissions",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_permission_id")
    )
    private Set<FmUserRolePermissions> rolePermission;*/

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id") // Points to the user_id column in user_role_permissions
    private Set<FmUserRolePermissions> userRolePermissions;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.isActive == null) this.isActive = "N";
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return userRolePermissions.stream()
                .map(urp -> urp.getRolePermission().getRole().getRoleName())
                .filter(Objects::nonNull)
                .distinct()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isEnabled() {
        return "Y".equalsIgnoreCase(this.isActive);
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }
}
