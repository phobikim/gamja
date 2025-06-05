package com.phobi.gamja.util;
public class SecurityUtil {

    public static String sanitize(String input) {
        if (input == null) return null;
        return input
                .replaceAll("&", "&amp;")
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\"", "&quot;")
                .replaceAll("'", "&#x27;")
                .replaceAll("/", "&#x2F;");
    }
}
