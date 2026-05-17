package com.company.directory.service;

import org.mindrot.jbcrypt.BCrypt;

import java.security.SecureRandom;

/**
 * BCrypt password hashing and a human-readable random-password generator.
 *
 * <p>All methods are static — this class is not meant to be instantiated.
 *
 * <p>BCrypt is used because it is slow by design (cost factor built into the
 * hash), which makes brute-force and rainbow-table attacks impractical.
 *
 * <p>The generated passwords use a curated character set that avoids visually
 * ambiguous characters ({@code O}, {@code 0}, {@code I}, {@code l}, {@code 1})
 * so they can be safely read aloud or written on paper without confusion.
 */
public final class PasswordUtil {

    // No ambiguous characters (O 0 I l 1); shell-safe symbols.
    private static final String CHARSET =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789@#$%&*?+-";
    private static final int LENGTH = 14;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() { }

    public static String generateReadablePassword() {
        StringBuilder sb = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            sb.append(CHARSET.charAt(RANDOM.nextInt(CHARSET.length())));
        }
        return sb.toString();
    }

    public static String hash(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    public static boolean matches(String rawPassword, String hash) {
        return BCrypt.checkpw(rawPassword, hash);
    }
}
