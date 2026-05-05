package com.jippy.foodandmart.mapper;

import com.jippy.foodandmart.entity.FmEmployee;

import java.time.LocalDateTime;

/**
 * Static utility class for creating {@link FmEmployee} entities from raw fields.
 *
 * <p>Why not accept a DTO: employees in this system are always created as a
 * side effect of merchant or outlet onboarding. There is no dedicated
 * "Create Employee" request DTO — the values come directly from the
 * merchant/outlet registration data.</p>
 */
public final class FmEmployeeMapper {

    /**
     * Private constructor — static utility class, must not be instantiated.
     */
    private FmEmployeeMapper() {}

    /**
     * Creates a new {@link FmEmployee} entity from individual field values.
     *
     * <p>Why trim name and email: user-supplied strings from registration forms
     * or file uploads may have extra whitespace. Trimming prevents duplicate
     * records that differ only by whitespace.</p>
     *
     * <p>Why lowercase email: the email column does not have a case-insensitive
     * unique index. Storing always-lowercase ensures lookups won't miss
     * records due to case differences.</p>
     *
     * <p>isActive defaults to "Y" because a newly created employee record
     * is always active — it is only deactivated via a separate admin action.</p>
     *
     * @param employeeName the employee's full name (will be trimmed)
     * @param email        the employee's email address (will be lowercased and trimmed)
     * @param mobileNumber the employee's mobile number (will be trimmed)
     * @param createdBy    the ID of the admin/user who created this record (audit trail)
     * @return a transient {@link FmEmployee} entity ready to persist
     */
    public static FmEmployee toEntity(String employeeName, String email, String mobileNumber, Integer createdBy) {
        FmEmployee entity = new FmEmployee();
        entity.setEmployeeName(employeeName != null ? employeeName.trim() : null);
        // Normalise email to lowercase for consistent storage
        entity.setEmail(email != null ? email.toLowerCase().trim() : null);
        entity.setMobileNumber(mobileNumber != null ? mobileNumber.trim() : null);
        // New employees are always active — deactivation is a separate action
        entity.setIsActive("Y");
        entity.setCreatedAt(LocalDateTime.now());
        entity.setCreatedBy(createdBy);
        return entity;
    }
}
