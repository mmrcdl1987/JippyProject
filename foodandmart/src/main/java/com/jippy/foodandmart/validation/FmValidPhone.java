package com.jippy.foodandmart.validation;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validates that a phone number is a 10-digit Indian mobile number
 * (optionally prefixed with +91 or 91).
 *
 * Usage:  @ValidPhone  on any String field in a DTO.
 */
@Documented
@Constraint(validatedBy = FmValidPhone.Validator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface FmValidPhone {

    String message() default "Phone must be a valid 10-digit Indian mobile number";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<FmValidPhone, String> {

        private static final java.util.regex.Pattern PHONE_PATTERN =
                java.util.regex.Pattern.compile("^(\\+91|91)?[6-9]\\d{9}$");

        @Override
        public boolean isValid(String value, ConstraintValidatorContext ctx) {
            if (value == null || value.isBlank()) return true; // use @NotBlank separately
            return PHONE_PATTERN.matcher(value.trim()).matches();
        }
    }
}
