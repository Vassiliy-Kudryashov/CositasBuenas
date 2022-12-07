package cositas.buenas.gitlog2picture;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Gitlog2Picture {
    static final SimpleDateFormat GIT_FORMAT = new SimpleDateFormat("E MMM d HH:mm:ss yyyy Z", Locale.getDefault());
    static final SimpleDateFormat SIMPLE_FORMAT = new SimpleDateFormat("dd.MM.yy", Locale.getDefault());
    public static final int MIN_REVISIONS_NUMBER = 5;
    private static final double MIN_COMMITS_PER_DAY = 0.1;
    static Map<String, String> diacritics = new HashMap<>();

    static {
        diacritics.put("ž", "z");
        diacritics.put("ä", "a");
        diacritics.put("ł", "l");
    }

    static List<String> NAMES = Arrays.asList(
            ("alena,ann,anna,alexey,arman,alexander,andrey,arsen,artem,artemii,anton,alex,alexandrina," +
                    "bas," +
                    "constantine,constantin," +
                    "denis,dmitry,dmitrii,dave,dmitri,dennis,dima," +
                    "ekaterina,elena,evelina,egor,eugene," +
                    "gleb,gregory," +
                    "hani," +
                    "ivan,igor,irina,ilya," +
                    "jeb,justin," +
                    "konstantin,kirill," +
                    "mikhail,maxim,michael,mark,maria," +
                    "nikita,nikolay," +
                    "olesya,olga," +
                    "pavel,peter," +
                    "ralf,roman,rustam," +
                    "saveliy,sergey,sascha,sofya,sergei,serge,stanislav," +
                    "vadim,vassiliy,viktoriia,vyacheslav,victor,vladislav,yury,valeriy," +
                    "zarina,zalim")
                    .split(","));

    static Properties config = new Properties();
    static Properties dictionary = new Properties();
    static List<String> authorsToSkip = new ArrayList<>();

    private static void readLines(File source, List<String> target) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(source))) {
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                target.add(line);
            }
        }
    }

    public static void main(String[] args) throws IOException, ParseException, InterruptedException {
        try (FileReader reader = new FileReader(args[0])) {
            config.load(reader);
        }
        try (FileReader reader = new FileReader(config.getProperty("synonyms"))) {
            dictionary.load(reader);
        }

        authorsToSkip.addAll(Arrays.asList(config.getProperty("authors_to_skip").split(",")));

        String[] roots = config.getProperty("roots").split(",");
        File logFile = new File(config.getProperty("title") + ".gitlog.txt");
        for (int i = 0; i < roots.length; i++) {
            String root = roots[i];
            String[] details = root.split("\\|");
            String[] params = null;
            if (details.length == 2) {
                root = details[0];
                params = details[1].split(" ");
            }
            File rootDir = new File(root);
            ProcessBuilder.Redirect redirect = i == 0 ? ProcessBuilder.Redirect.to(logFile) : ProcessBuilder.Redirect.appendTo(logFile);
            List<String> commands = new ArrayList<>(Arrays.asList("git", "log", "--all"));
            if (params != null) commands.addAll(Arrays.asList(params));
            Process process = new ProcessBuilder(commands)
                    .redirectError(ProcessBuilder.Redirect.INHERIT).redirectOutput(redirect).directory(rootDir).start();
            process.waitFor(1, TimeUnit.MINUTES);
        }


        List<String> lines = new ArrayList<>();
        readLines(logFile, lines);
        if (!logFile.delete()) logFile.deleteOnExit();

        List<Revision> revisions = new ArrayList<>();
        String tmpAuthor = null;
        String tmpDate;
        for (String line : lines) {
            if (line.startsWith("Author:")) tmpAuthor = line.substring(8);
            if (line.startsWith("Date:")) {
                tmpDate = line.substring(5).trim();
                Revision revision = new Revision(tmpAuthor, GIT_FORMAT.parse(tmpDate));
                revisions.add(revision);
            }
        }
        Collections.sort(revisions);
        List<AuthorRange> ranges = new ArrayList<>();
        AuthorRange currentRange = null;
        int counter = 0;
        for (int i = 0; i < revisions.size(); i++) {
            Revision revision = revisions.get(i);
            if (authorsToSkip.contains(revision.author)) {
                continue;
            }

            if (currentRange == null) {
                currentRange = new AuthorRange(revision.author, revision.date);
                continue;
            }
            if (currentRange.author.equals(revision.author)) {
                currentRange.addDate(revision.date);
            } else {
                ranges.add(currentRange);
                currentRange = new AuthorRange(revision.author, revision.date);
            }
            if (i == revisions.size() - 1) {
                ranges.add(currentRange);
            }
        }

        Collections.sort(ranges);
        ranges.removeIf(authorRange -> authorRange.dates.size() < MIN_REVISIONS_NUMBER);
        ranges.removeIf(authorRange -> authorRange.getCommitsPerDay() < MIN_COMMITS_PER_DAY);
        counter = 1;
        for (AuthorRange range : ranges) {
            System.out.println(counter + ". " + range);
            counter++;
        }
        File picture = new File(config.getProperty("title") + ".png");
        saveToImage(ranges, picture);
        Desktop.getDesktop().open(picture);
    }

    private static class Revision implements Comparable<Revision> {
        private String author;
        private final Date date;

        public Revision(String author, Date date) {
            this.author = parseNameAndSurname(author);
            this.author = dictionary.getProperty(this.author, this.author);
            this.date = date;
        }

        @Override
        public int compareTo(Revision o) {
            int i = author.compareTo(o.author);
            if (i != 0) {
                return i;
            }
            return date.compareTo(o.date);
        }

        private static String parseNameAndSurname(String raw) {
            raw = raw.toLowerCase().replaceAll("\\d+", "").replace("+github", "");
            for (Map.Entry<String, String> entry : diacritics.entrySet()) {
                raw = raw.replaceAll(entry.getKey(), entry.getValue());
            }
            String[] words = raw.replace(".", " ").split(" ");
            if (words.length > 2) {
                String fromTwoWords = fromTwoWords(words[0], words[1]);
                if (fromTwoWords != null) return fromTwoWords;
            }
            int emailStart = raw.indexOf('<');
            int emailEnd = raw.indexOf('@');
            if (emailStart >= 0 && emailEnd > emailStart) {
                String r = raw.substring(emailStart + 1, emailEnd);
                String[] words2 = r.split("\\.");
                if (words2.length >= 2) {
                    String fromTwoWords = fromTwoWords(words2[0], words2[1]);
                    if (fromTwoWords != null) return fromTwoWords;
                }
                String s = capitalize(r.replaceAll("\\d+\\+?", ""));
                if (words2.length >= 2 && s.length() > 3)
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

        @Override
        public String toString() {
            return author + " " + SIMPLE_FORMAT.format(date);
        }
    }

    private static class AuthorRange implements Comparable<AuthorRange> {
        private final String author;
        private TreeSet<Date> dates = new TreeSet<>();
        private Date firstDate;
        private Date lastDate;

        public AuthorRange(String author, Date date) {
            this.author = author;
            this.firstDate = date;
            this.lastDate = date;
        }

        public void addDate(Date date) {
            dates.add(date);
            if (firstDate.after(date)) firstDate = date;
            if (lastDate.before(date)) lastDate = date;
        }

        public int getDays() {
            return (int)((lastDate.getTime() - firstDate.getTime()) / 86400000L);
        }

        public double getCommitsPerDay() {
            int days = getDays();
            return days > 0 ? (double)dates.size() / days : 1;
        }

        @Override
        public int compareTo(AuthorRange o) {
            int i = firstDate.compareTo(o.firstDate);
            if (i != 0) return i;
            return -lastDate.compareTo(o.lastDate);
        }

        @Override
        public String toString() {
            return author + " " + SIMPLE_FORMAT.format(firstDate) + " - " + SIMPLE_FORMAT.format(lastDate);
        }
    }

    static void saveToImage(List<AuthorRange> ranges, File output) throws IOException, ParseException {
        long startTime = ranges.get(0).firstDate.getTime();
        long totalTime = ranges.get(ranges.size() - 1).lastDate.getTime() - startTime;

        Collections.reverse(ranges);

        int margin = 100;
        int h = Math.min(Short.MAX_VALUE / 3 * 2 - 1, 2 * margin + ranges.size() * 50);
        int w = h * 3 / 2;
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 25));
        g.fillRect(0, 0, w + 1, h + 1);
        int innerW = w - 7 * margin;//timeline
        float pixelsPerMs = (float) innerW / totalTime;
        int innerH = h - 2 * margin;
        float step = (float) innerH / ranges.size();
        float arc = step / 10;

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
        while (calendar.getTime().before(new Date(startTime + totalTime + 86400000L * 30L))) {
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
            Date prevDate = null;
            for (Date date : range.dates) {
                if (prevDate != null) {
                    float x1 = margin + innerW * ((float) prevDate.getTime() - startTime) / totalTime;
                    float x2 = x1 + arc;
                    g.fill(new RoundRectangle2D.Float(x1, startY, x2 - x1, endY - startY, arc, arc));
                }
                prevDate = date;
            }
            g.setColor(Color.black);
            int days = range.getDays();
            double cpd = range.getCommitsPerDay();
            g.drawString("" + range.author + " (" + days + " days, \u03b7="+String.format("%.2f", cpd)+") ", endX + step, endY - arc);
        }
        g.setColor(Color.black);
        g.setFont(g.getFont().deriveFont(g.getFont().getSize() * 3.3f));
        g.drawString(config.getProperty("title") + " commits", 2 * margin, margin);

        ImageIO.write(image, "png", output);
    }
}
