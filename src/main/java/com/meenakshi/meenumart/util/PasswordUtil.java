package com.meenakshi.meenumart.util;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordUtil {

    private PasswordUtil() {}

    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }

    public static boolean matches(String plainPassword, String hash) {
        return BCrypt.checkpw(plainPassword, hash);
    }
}
