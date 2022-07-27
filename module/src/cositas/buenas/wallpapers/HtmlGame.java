package cositas.buenas.wallpapers;

import cositas.buenas.util.FileUtil;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class HtmlGame {
    static String SCRIPT =
            "<script type=\"text/javascript\">\n" +
                    "  var expected=0;\n" +
                    "  function process(i) {\n" +
                    "console.log(i+'/' + expected);\n" +
                    "    if (expected==i) {\n" +
                    "      document.getElementById('abc_'+i).style.border=\"solid 1px\";\n" +
                    "      expected++;\n" +
                    "    } else {\n" +
                    "      for (j = 0; j < expected;j++) {\n" +
                    "        document.getElementById('abc_'+j).style.border=\"none\";\n" +
                    "      }\n" +
                    "      expected = 0;\n" +
                    "    }\n" +
                    "  }\n" +
                    "</script>\n";

    public static void generateHTML(File dir, int width, int height, File imageFile, Map<String, ABCWallpapers.MultiRect> multiRectMap) throws IOException {
        File out = new File(dir, imageFile.getName() + ".html");
        StringBuilder sb = new StringBuilder("<html>\n<head>\n" + SCRIPT + "</head>\n<body style=\"margin:0;\">\n");
        sb.append("<img width=\"100%\" height=\"100%\" src=\"").append(imageFile.getName()).append("\" usemap=\"#abc\">\n");
        int counter = 0;
        ArrayList<String> ordered = new ArrayList<>(multiRectMap.keySet());
        ordered.sort((o1, o2) -> {
            if (o1.length() > 1) return -1;
            if (o2.length() > 1) return 1;
            return o1.compareTo(o2);
        });

        for (String key : ordered) {
            ABCWallpapers.MultiRect multiRect = multiRectMap.get(key);
            Rectangle bounds = multiRect.getBounds();
            //todo coords in %%
            sb.append("<div id=\"abc_").append(counter).append("\" onClick=process('").append(counter).append("') style=\"position:fixed;")
                    .append("z-index:"+(10000 - bounds.height)).append(";")
                    .append("left:").append(100.0 * bounds.x / width).append("%;")
                    .append("top:").append(100.0 * bounds.y / height).append("%;")
                    .append("width:").append(100.0 * bounds.width / width).append("%;")
                    .append("height:").append(100.0 * bounds.height / height).append("%;\"></div>\n");
            counter++;
        }
        sb.append("</body>\n</html>");
        FileUtil.save(sb, out);
    }
}
