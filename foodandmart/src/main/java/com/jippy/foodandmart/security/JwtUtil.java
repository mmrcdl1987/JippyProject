package com.jippy.foodandmart.security;

/**
 * JWT utility placeholder.
 *
 * Replace this stub with a real implementation when adding JWT-based
 * authentication. Recommended library: io.jsonwebtoken:jjwt-api (0.11+).
 *
 * Typical implementation would:
 *  - generateToken(String username, String role) → String
 *  - validateToken(String token)                 → boolean
 *  - extractUsername(String token)               → String
 *  - extractRole(String token)                   → String
 *
 * Also add:
 *  - JwtAuthenticationFilter  (extends OncePerRequestFilter)
 *  - SecurityConfig           (@Configuration + SecurityFilterChain bean)
 *  - UserDetailsServiceImpl   (loads user from UserRepository)
 */
public class JwtUtil {

    // TODO: inject from application.yml
    private static final String SECRET_KEY = "REPLACE_WITH_256_BIT_SECRET";
    private static final long   EXPIRY_MS  = 86_400_000L; // 24 hours

    private JwtUtil() {}

    public static String generateToken(String username, String role) {
        throw new UnsupportedOperationException("JWT not yet implemented. Add jjwt dependency and implement.");
    }

    public static boolean validateToken(String token) {
        throw new UnsupportedOperationException("JWT not yet implemented.");
    }

    public static String extractUsername(String token) {
        throw new UnsupportedOperationException("JWT not yet implemented.");
    }
}
