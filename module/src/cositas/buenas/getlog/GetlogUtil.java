package cositas.buenas.getlog;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class GetlogUtil {
    private static final Map<String, String> diacritics = new HashMap<>();
    static List<String> NAMES = Arrays.asList(
            ("alena,ann,anna,alexey,arman,alexander,andrey,arsen,artem,artemii,anton,alex,alexandrina," +
                    "bas," +
                    "constantine,constantin,cyril" +
                    "denis,dmitry,dmitrii,dave,dmitri,dennis,dima,darja" +
                    "ekaterina,elena,evelina,egor,eugene," +
                    "gleb,gregory," +
                    "hani," +
                    "ivan,igor,irina,ilya," +
                    "jeb,justin,julia" +
                    "konstantin,kirill," +
                    "mikhail,maxim,michael,mark,maria," +
                    "nikita,nikolay," +
                    "olesya,olga," +
                    "pavel,peter," +
                    "ralf,roman,rustam," +
                    "saveliy,sergey,sascha,sofya,sergei,serge,stanislav," +
                    "vadim,vasili,vassiliy,viktoriia,vyacheslav,victor,vladislav,yury,valeriy," +
                    "zarina,zalim")
                    .split(","));

    static {
        diacritics.put("ž", "z");
        diacritics.put("ä", "a");
        diacritics.put("á", "a");
        diacritics.put("ł", "l");
        diacritics.put("ø", "o");
        diacritics.put("é", "e");
    }

    static File getRootDir(File gitLogDir, final String root) throws IOException, InterruptedException {
        if (root.startsWith("http") || root.startsWith("git@")) {
            String file = getProjectNameByUrl(root);
            File projectDir = new File(gitLogDir, file);
            File[] files = projectDir.listFiles();
            if (files == null || files.length == 0) {
                Process process = new ProcessBuilder("git", "clone", root)
                        .inheritIO().directory(gitLogDir).start();
                process.waitFor(1, TimeUnit.HOURS);
            } else {
                long start = System.currentTimeMillis();
                System.out.print("Updating project...");
                Process process = new ProcessBuilder("git", "pull", "--rebase", "-q")
                        .inheritIO().directory(projectDir).start();
                process.waitFor(1, TimeUnit.HOURS);
                System.out.println("done in " + (System.currentTimeMillis() - start) + "ms");
            }
            return projectDir;
        }
        return new File(root);
    }

    static String getProjectNameByUrl(String root) {
        String file = root.substring(root.lastIndexOf("/") + 1);
        if (file.endsWith(".git")) file = file.substring(0, file.length() - 4);
        return file;
    }

    static String parseEmail(String raw) {
        int start = raw.lastIndexOf('<');
        if (start == -1) return null;
        int stop = raw.indexOf('>', start);
        if (stop == -1) return null;
        return raw.substring(start + 1, stop);
    }

    static String parseNameAndSurname(String raw) {
        raw = raw
                .toLowerCase()
                .replaceAll("\\s{2,}", " ")
                .replaceAll("@{2,}", "@")
                .replaceAll("\\d+", "")
                .replace("+github", "");
        for (Map.Entry<String, String> entry : diacritics.entrySet()) {
            raw = raw.replaceAll(entry.getKey(), entry.getValue());
        }
        String[] words = raw.replace(".", " ").split(" ");
        if (words.length > 2) {
            String fromTwoWords = fromTwoWords(words[0], words[1]);
            if (fromTwoWords != null) return fromTwoWords;
        }
        int emailStart = raw.indexOf('<');
        int emailEnd = raw.indexOf('@', emailStart + 1);
        if (emailStart >= 0 && emailEnd > emailStart) {
            String r = raw.substring(emailStart + 1, emailEnd);
            String[] words2 = r.split("\\.");
            if (words2.length >= 2) {
                String fromTwoWords = fromTwoWords(words2[0], words2[1]);
                if (fromTwoWords != null) return fromTwoWords;
            }
            String s = capitalize(r.replaceAll("\\d*\\+?", "").replaceAll("@.*", "").replaceAll("\\(", ""));
            if (/*words2.length >= 2 &&*/ s.length() > 3)
                return s;
        }
        String[] additionalSplit = words[0].split("_");
        if (additionalSplit.length == 2) {
            return capitalize(additionalSplit[0]) + " " + capitalize(additionalSplit[1]);
        }
        return capitalize(words[0]);
        //Pavel V
        //B A
        //Kirill
        //No Reply@jetbrains
        //Vlad -> Vlad20012 -> beskvlad@gmail.com
        //Unknown
    }

    private static String fromTwoWords(String first, String second) {
        if (first.matches("[a-z]+") && second.matches("[a-z]+")) {
            if (NAMES.contains(second)) {
                return capitalize(second) + " " + capitalize(first);
            }
            return capitalize(first) + " " + capitalize(second);
        }
        return null;
    }

    private static String capitalize(String s) {
        if (s.length() < 2) return s.toUpperCase();
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    static void saveToImage(String title, List<AuthorRange> ranges, File output) throws IOException, ParseException {
        long startTime = ranges.get(0).firstDate.getTime();
        Date theVeryLastDate = new Date(startTime);
        for (AuthorRange range : ranges) {
            if (theVeryLastDate.before(range.lastDate)) theVeryLastDate = range.lastDate;
        }
        long totalTime = theVeryLastDate.getTime() - startTime;

        Collections.reverse(ranges);

        int margin = 100;
        int h = Math.min(Short.MAX_VALUE, Math.max(600, Math.min(Short.MAX_VALUE / 3 * 2 - 1, 2 * margin + ranges.size() * 50)));
        int w = Math.min(Short.MAX_VALUE, Math.max((int) (totalTime / 86400000L) + margin * 7, h * 3 / 2));
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 25));
        g.fillRect(0, 0, w + 1, h + 1);
        final int innerW = w - 7 * margin;//timeline
        final float pixelsPerMs = (float) innerW / totalTime;
        final int innerH = h - 2 * margin;
        final float step = (float) innerH / ranges.size();
        final float arc = step / 10;

        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        Color[] stripes = new Color[]{new Color(0, 0, 0, 33), new Color(0, 0, 0, 66)};
        Color[] monthsStripes = new Color[]{
                new Color(0, 0, 0, 33), new Color(0, 0, 0, 12),
                new Color(89, 181, 224, 66), new Color(74, 147, 198, 66), new Color(89, 181, 224, 66),
                new Color(164, 204, 105, 66), new Color(69, 178, 119, 66), new Color(164, 204, 105, 66),
                new Color(233, 136, 89, 66), new Color(245, 193, 108, 66), new Color(233, 136, 89, 66),
                new Color(0, 0, 0, 12)
        };
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startTime);
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        int index = 0;
        while (calendar.getTime().before(new Date(theVeryLastDate.getTime() + 86400000L * 30L))) {
            float startX = margin + (calendar.getTimeInMillis() - startTime) * pixelsPerMs;
            calendar.add(Calendar.MONTH, 1);
            float endX = margin + (calendar.getTimeInMillis() - startTime) * pixelsPerMs;
//            g.setColor(stripes[index]);
            g.setColor(monthsStripes[calendar.get(Calendar.MONTH)]);
            g.fill(new Rectangle2D.Float(startX, 0, endX - startX, h));
            if (calendar.get(Calendar.MONTH) == Calendar.JANUARY) {
                g.setColor(Color.LIGHT_GRAY);
                g.draw(new Line2D.Float(endX, 0, endX, h));
                g.setColor(Color.BLUE);
                g.drawString("" + calendar.get(Calendar.YEAR), endX + arc, h - margin / 2);
            }
            index = 1 - index;
        }
        GradientPaint fillPaint = new GradientPaint(margin, margin, new Color(0x2e6b39),
                w - margin, h - margin, new Color(0x6ccf64));
        for (int i = 0; i < ranges.size(); i++) {
            AuthorRange range = ranges.get(i);
            g.setPaint(fillPaint);

            float startX = margin + innerW * ((float) range.firstDate.getTime() - startTime) / totalTime;
            float endX = margin + innerW * ((float) range.lastDate.getTime() - startTime) / totalTime;
            float startY = margin + step * i + arc;
            float endY = margin + step * (i + 1) - arc;
//            Date prevDate = null;
            for (Date date : range.dates) {
                float x1 = margin + innerW * ((float) date.getTime() - startTime) / totalTime;
//                if (prevDate != null) {
//                    float x2 = x1 + arc;
//                    g.fill(new RoundRectangle2D.Float(x1, startY, x2 - x1, endY - startY, arc, arc));
                g.draw(new Line2D.Float(x1, startY, x1, endY));
//                }
//                prevDate = date;
            }
            g.draw(new Rectangle2D.Float(startX, startY, endX - startX, endY - startY));//outer frame of the author's timeline
            g.setColor(Color.black);
            int days = range.getDays();
            double cpd = range.getCommitsPerDay();
            g.drawString("" + range.author + " (" + days + " days, \u03b7=" + String.format("%.2f", cpd) + ") ", endX + step, endY - arc);
        }
        g.setColor(Color.black);
        g.setFont(g.getFont().deriveFont(g.getFont().getSize() * 3.3f));
        g.drawString(title + " commits", 2 * margin, margin);

        ImageIO.write(image, "png", output);
    }
}
