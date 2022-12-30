package cositas.buenas.wallpapers;

import java.awt.*;
import java.text.Format;
import java.text.SimpleDateFormat;

public class ABCConfig {
    public static final Format FORMAT = new SimpleDateFormat("HH:mm:ss");
    public static final String ABC = createABC(new char[][]{{'0', '9'}, {'A', 'Z'}, {'a', 'z'}});

    public static final Color ABC_COLOR = new Color(0, 0, 0, 16);//6
    public static final double GAP_SCREEN_RATIO = .005;
    public static final double LOCAL_GAP_RATIO = .05;
    public static final boolean USE_LOCAL_GAP_RATIO = false;
    public static final int OUTER_MARGIN_DELIMITER = 9;//100;

    public static final  boolean CLEAR_PREVIOUS_RESULTS = false;

    public static final  boolean SKIP_EXISTING_FILES = true;

    //Do we fill the area as much as possible with random chars use each character just once
    public static final  boolean ONE_OF_EACH = true;

    public static final  boolean ONE_OF_EACH_IN_LEVEL = true;

    public static final int MULTIRECT_SQUARE_SIZE = 3;//50;

    public static final  boolean HTML_VERSION = false;

    public static final  boolean EMBED_FONT_NAME = true;

    public static final  double INITIAL_FONT_RATIO = .5;
    public static final  double MIN_FONT_RATIO = 1d / 256;

    public static final int GRID_POSITION_STEP = 1;//100;

    static final boolean DEBUG = false;

    private static String createABC(char[][] ranges) {
        StringBuilder sb = new StringBuilder();
        for (char[] range : ranges) {
            for (char ch = range[0]; ch <= range[1]; ch++) sb.append(ch);
        }
//        System.out.println("ABC: " + sb);
        return sb.toString();
    }

}
