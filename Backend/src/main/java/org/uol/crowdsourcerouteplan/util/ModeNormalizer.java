package org.uol.crowdsourcerouteplan.util;

import java.util.Locale;

public class ModeNormalizer {
    public static String normalize(String input, String defaultMode) {
        if (input == null || input.isBlank()) return defaultMode;
        String s = input.toLowerCase(Locale.ROOT).trim();

        if (s.equals("driving-car") || s.equals("driving-hgv")
                || s.equals("foot-walking") || s.equals("cycling-regular")) return s;

        if (s.contains("walk") || s.contains("foot")) return "foot-walking";
        if (s.contains("cycle") || s.contains("bike")) return "cycling-regular";
        if (s.contains("truck") || s.contains("hgv") || s.contains("lorry")) return "driving-hgv";
        if (s.contains("car") || s.contains("drive") || s.contains("taxi")) return "driving-car";

        return defaultMode;
    }
}
