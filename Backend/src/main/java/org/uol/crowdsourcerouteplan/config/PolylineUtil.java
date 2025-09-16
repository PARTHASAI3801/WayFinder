package org.uol.crowdsourcerouteplan.config;

import java.util.ArrayList;
import java.util.List;

public class PolylineUtil {
    public static List<double[]> decode(final String encoded) {
        List<double[]> path = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lon = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do { b = encoded.charAt(index++) - 63; result |= (b & 0x1f) << shift; shift += 5; } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0; result = 0;
            do { b = encoded.charAt(index++) - 63; result |= (b & 0x1f) << shift; shift += 5; } while (b >= 0x20);
            int dlon = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lon += dlon;

            path.add(new double[] { lat / 1e5, lon / 1e5 });
        }
        return path;
    }

    public static String encode(final List<double[]> path) {
        long lastLat = 0, lastLon = 0;
        StringBuilder result = new StringBuilder();

        for (double[] p : path) {
            long lat = Math.round(p[0] * 1e5);
            long lon = Math.round(p[1] * 1e5);

            long dLat = lat - lastLat;
            long dLon = lon - lastLon;
            lastLat = lat;
            lastLon = lon;

            encodeSignedNumber(dLat, result);
            encodeSignedNumber(dLon, result);
        }
        return result.toString();
    }

    private static void encodeSignedNumber(long num, StringBuilder result) {
        long sgnNum = num << 1;
        if (num < 0) sgnNum = ~sgnNum;
        while (sgnNum >= 0x20) {
            long nextValue = (0x20 | (sgnNum & 0x1f)) + 63;
            result.append((char) (nextValue));
            sgnNum >>= 5;
        }
        result.append((char) (sgnNum + 63));
    }
}
