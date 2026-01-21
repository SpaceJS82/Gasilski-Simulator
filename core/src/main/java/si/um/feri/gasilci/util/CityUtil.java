package si.um.feri.gasilci.util;

public class CityUtil {

    public static int[] getTileDimensions(String citySize) {
        if (citySize == null) {
            // Default to medium if size not specified
            return new int[]{32, 18};
        }

        switch (citySize.toLowerCase()) {
            case "small":
                return new int[]{24, 14};
            case "medium":
                return new int[]{32, 18};
            case "large":
                return new int[]{40, 24};
            default:
                // Default to medium for unknown sizes
                return new int[]{32, 18};
        }
    }
}
