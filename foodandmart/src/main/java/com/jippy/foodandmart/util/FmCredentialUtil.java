package com.jippy.foodandmart.util;

/**
 * Utility for generating portal login credentials for merchants and outlets.
 *
 * <pre>
 * Merchant:
 *   username = firstName (lowercase, no spaces) + last 4 digits of phone
 *   password = first 4 chars of email         + last 4 digits of phone
 *
 * Outlet:
 *   username = first 4 letters of outlet name (lowercase, letters only) + last 4 digits of phone
 *   password = same as username (initial password)
 * </pre>
 */
public final class FmCredentialUtil {

    private FmCredentialUtil() {}

    public static String generateMerchantUsername(String firstName, String phone) {
        String namePart  = firstName.trim().toLowerCase().replaceAll("\\s+", "");
        String phonePart = last4(phone);
        return namePart + phonePart;
    }

    public static String generateMerchantPassword(String email, String phone) {
        String emailPart = email.toLowerCase().trim();
        String prefix    = emailPart.length() >= 4 ? emailPart.substring(0, 4) : emailPart;
        return prefix + last4(phone);
    }

    public static String generateOutletLoginId(String outletName, String phone) {
        String namePart = outletName.toLowerCase().replaceAll("[^a-z]", "");
        namePart = namePart.length() >= 4 ? namePart.substring(0, 4) : namePart;
        return namePart + last4(phone);
    }

    public static String generateOutletPassword(String outletName, String phone) {
        return generateOutletLoginId(outletName, phone);
    }

    private static String last4(String phone) {
        if (phone == null) return "0000";
        String clean = phone.replaceAll("\\D", "");
        return clean.length() >= 4 ? clean.substring(clean.length() - 4) : clean;
    }
}
