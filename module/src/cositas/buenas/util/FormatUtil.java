package cositas.buenas.util;

public class FormatUtil {
    public static String formatWithLeadingZeros(int i, int zeros) {
        return String.format("%0" + zeros + "d", i);
    }

    public static String formatWithPrecision(double d, int precision) {
        return String.format("%." + precision + "f", d);
    }
}
