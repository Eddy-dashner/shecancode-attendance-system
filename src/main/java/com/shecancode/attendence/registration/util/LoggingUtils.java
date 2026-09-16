package com.shecancode.attendence.registration.util;

import org.apache.commons.text.StringEscapeUtils;

/**
 * Utility class for safe logging operations to prevent XSS attacks
 */
public class LoggingUtils {
    
    public static String sanitizeForLogging(String input) {
        if (input == null) {
            return "null";
        }
        return StringEscapeUtils.escapeHtml4(input);
    }
    
    public static String sanitizeForLogging(String input, int maxLength) {
        String sanitized = sanitizeForLogging(input);
        if (sanitized.length() > maxLength) {
            return sanitized.substring(0, maxLength) + "...";
        }
        return sanitized;
    }
}
