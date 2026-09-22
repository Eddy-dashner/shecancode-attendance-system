package com.shecancode.attendence.auth.util;

import java.util.Locale;

public final class EmailUtils {

    private EmailUtils() {
    }

    /** Trims and lower-cases an email so it is unique and matches regardless of how it was typed. */
    public static String normalize(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
