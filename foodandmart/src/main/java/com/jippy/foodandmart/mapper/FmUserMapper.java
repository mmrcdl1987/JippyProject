package com.jippy.foodandmart.mapper;

import com.jippy.foodandmart.entity.FmUser;

import java.time.LocalDateTime;

/**
 * Static utility class for creating {@link FmUser} portal-login entities.
 *
 * <p>Why a separate mapper: user creation is always a side effect of
 * merchant or outlet onboarding. Centralising the field mapping here keeps
 * the service layer clean and makes it easy to add future fields (e.g.
 * a profile picture URL) in one place.</p>
 */
public final class FmUserMapper {

    /**
     * Private constructor — static utility class, must not be instantiated.
     */
    private FmUserMapper() {}

    /**
     * Creates a new {@link FmUser} entity for portal login.
     *
     * <p>Why accept encodedPassword instead of raw password: the service layer
     * is responsible for hashing or encoding passwords before calling this mapper.
     * The mapper has no knowledge of the encoding algorithm, keeping concerns
     * separated.</p>
     *
     * <p>Why pass roleId and userType separately: the role and user type values
     * come from {@code AppConstants} in the service layer. Having the mapper
     * accept them as plain parameters avoids coupling the mapper to AppConstants.</p>
     *
     * <p>isActive defaults to "Y" — new portal users are immediately active and
     * can log in right after onboarding.</p>
     *
     * @param username        the generated login username (trimmed by service before passing in)
     * @param encodedPassword the password (plain or encoded, depending on calling service)
     * @param roleId          the role FK (e.g. ROLE_ID_MERCHANT or ROLE_ID_OUTLET from AppConstants)
     * @param employeeId      the FK linking this user to their employee record
     * @param userType        the user type string (e.g. "MERCHANT" or "OUTLET")
     * @param createdBy       the ID of the admin who triggered this creation (audit trail)
     * @return a transient {@link FmUser} entity ready to persist
     */
    public static FmUser toEntity(String username, String encodedPassword, Integer roleId,
                                  Integer employeeId, String userType, Integer createdBy) {
        FmUser entity = new FmUser();
        entity.setUsername(username != null ? username.trim() : null);
        entity.setPassword(encodedPassword);
       // entity.setRoleId(roleId);
        entity.setEmployeeId(employeeId);
        entity.setUserType(userType);
        // New users are immediately active after onboarding
        entity.setIsActive("Y");
        entity.setCreatedAt(LocalDateTime.now());
        entity.setCreatedBy(createdBy);
        return entity;
    }
}
