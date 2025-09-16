package org.uol.crowdsourcerouteplan.model;

import java.util.ArrayList;
import java.util.List;

public final class PolylineCodec {
    private PolylineCodec() {}

    public static List<double[]> decode(String encoded) {
        List<double[]> path = new ArrayList<>();
        int index = 0, lat = 0, lng = 0;
        while (index < encoded.length()) {
            int[] r1 = chunk(encoded, index); index = r1[2]; lat += r1[0];
            int[] r2 = chunk(encoded, index); index = r2[2]; lng += r2[0];
            path.add(new double[] { lat / 1e5, lng / 1e5 });
        }
        return path;
    }
    private static int[] chunk(String s, int index) {
        int result = 0, shift = 0, b;
        do {
            b = s.charAt(index++) - 63;
            result |= (b & 0x1f) << shift;
            shift += 5;
        } while (b >= 0x20 && index < s.length());
        int delta = ((result & 1) != 0) ? ~(result >> 1) : (result >> 1);
        return new int[] { delta, b, index };
    }
    public static String encode(List<double[]> path) {
        StringBuilder out = new StringBuilder();
        long lastLat = 0, lastLng = 0;
        for (double[] p : path) {
            long lat = Math.round(p[0] * 1e5);
            long lng = Math.round(p[1] * 1e5);
            write(out, lat - lastLat); write(out, lng - lastLng);
            lastLat = lat; lastLng = lng;
        }
        return out.toString();
    }
    private static void write(StringBuilder out, long v) {
        v = (v < 0) ? ~(v << 1) : (v << 1);
        while (v >= 0x20) { out.append((char)((0x20 | (v & 0x1f)) + 63)); v >>= 5; }
        out.append((char)(v + 63));
    }
}

