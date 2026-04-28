package com.jippy.foodandmart.validation;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validates Indian IFSC code format: 4 uppercase letters + 0 + 6 alphanumeric chars.
 * Example: SBIN0001234
 */
@Documented
@Constraint(validatedBy = FmValidIfsc.Validator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface FmValidIfsc {

    String message() default "IFSC code must be in format: 4 letters + 0 + 6 alphanumeric chars (e.g. SBIN0001234)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<FmValidIfsc, String> {

        private static final java.util.regex.Pattern IFSC_PATTERN =
                java.util.regex.Pattern.compile("^[A-Z]{4}0[A-Z0-9]{6}$");

        @Override
        public boolean isValid(String value, ConstraintValidatorContext ctx) {
            if (value == null || value.isBlank()) return true;
            return IFSC_PATTERN.matcher(value.trim().toUpperCase()).matches();
        }
    }
}
