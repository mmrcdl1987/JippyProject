package com.jippy.foodandmart.util;

/**
 * Generic string helpers used across the application.
 */
public final class FmStringUtil {

    private FmStringUtil() {}

    public static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    public static String safe(String s) {
        return s != null ? s.trim() : "";
    }

    public static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }

    public static String normalise(String s) {
        return s != null ? s.trim().toUpperCase() : null;
    }

    public static String stripNonDigits(String s) {
        return s != null ? s.replaceAll("\\D", "") : null;
    }
}
