package com.shecancode.attendence.registration.util;

import org.apache.commons.text.StringEscapeUtils;

/**
 * Utility class for safe logging operations to prevent XSS attacks
 */
public class LoggingUtils {
    
    /**
     * Sanitizes input for safe logging by escaping HTML characters
     * @param input the input to sanitize
     * @return sanitized input safe for logging
     */
    public static String sanitizeForLogging(String input) {
        if (input == null) {
            return "null";
        }
        return StringEscapeUtils.escapeHtml4(input);
    }
    
    /**
     * Sanitizes and truncates input for safe logging
     * @param input the input to sanitize
     * @param maxLength maximum length after sanitization
     * @return sanitized and truncated input
     */
    public static String sanitizeForLogging(String input, int maxLength) {
        String sanitized = sanitizeForLogging(input);
        if (sanitized.length() > maxLength) {
            return sanitized.substring(0, maxLength) + "...";
        }
        return sanitized;
    }
}
