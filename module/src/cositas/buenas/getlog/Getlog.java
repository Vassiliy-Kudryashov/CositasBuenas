package cositas.buenas.getlog;

import java.awt.*;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static cositas.buenas.getlog.GetlogUtil.parseNameAndSurname;
import static cositas.buenas.getlog.GetlogUtil.saveToImage;
import static java.util.Optional.ofNullable;

public class Getlog {
    static final SimpleDateFormat GIT_FORMAT = new SimpleDateFormat("E MMM d HH:mm:ss yyyy Z", Locale.getDefault());
    static final SimpleDateFormat SIMPLE_FORMAT = new SimpleDateFormat("dd.MM.yy", Locale.getDefault());

    public static final int MIN_REVISIONS_NUMBER = 5;
    private static final double MIN_COMMITS_PER_DAY = 0.05;


//    static List<String> bots = new ArrayList<>();

    private static void readLines(File source, List<String> target) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(source))) {
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                target.add(line);
            }
        }
    }

    public static void main(String[] args) throws IOException, ParseException, InterruptedException {
        String target = args.length > 0 ? args[0] : null;
        if (target != null) {
            proceed(target);
        } else {
            List<String> targets = new ArrayList<>();
            readLines(new File("resources/getlog.targets"), targets);
            for (String s : targets) {
                if (s.isEmpty() || s.startsWith("#")) continue;
                proceed(s);
            }
        }
    }

    private static void proceed(String target) throws IOException, InterruptedException, ParseException {
        Properties config = new Properties();
        Properties aliases = new Properties();
        long start;
        File baseDir = new File(new File(System.getProperty("user.home")), ".getlog").getCanonicalFile();
        String title;
        if (target.contains("://") || target.startsWith("git@")) {
            config.put("roots", target);
            title = GetlogUtil.getProjectNameByUrl(target);
            config.put("aliases", "resources/" + title + ".aliases.properties");
        } else {
            try (FileReader reader = new FileReader(target)) {
                config.load(reader);
            }
            title = config.getProperty("title");
        }
        System.out.println("***");
        System.out.println("* Proceed with " + title);
        System.out.println("***");
        ofNullable(config.getProperty("aliases")).ifPresent(s -> {
            File file = new File(s);
            if (file.isFile()) {
                try (FileReader reader = new FileReader(s)) {
                    aliases.load(reader);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
//        ofNullable(config.getProperty("bots")).ifPresent(s -> bots.addAll(Arrays.asList(s.split(","))));

        String[] roots = config.getProperty("roots").split(",");
        File logFile = new File(baseDir, title + ".gitlog.txt");
        {
            Map<String, File> root2dir = new HashMap<>();
            for (String s : roots) {
                root2dir.put(s, GetlogUtil.getRootDir(baseDir, s));
            }

            start = System.currentTimeMillis();
            System.out.print("Preparing Git log...");
            for (int i = 0; i < roots.length; i++) {
                String root = roots[i];
                String[] details = root.split("\\|");
                String[] params = null;
                File rootDir = root2dir.get(root);
                if (details.length == 2) {
                    root = details[0];
                    params = details[1].split(" ");
                    rootDir = new File(root);
                }
                ProcessBuilder.Redirect redirect = i == 0 ? ProcessBuilder.Redirect.to(logFile) : ProcessBuilder.Redirect.appendTo(logFile);
                List<String> commands = new ArrayList<>(Arrays.asList("git", "log", "--all"));
                if (params != null) commands.addAll(Arrays.asList(params));
                Process process = new ProcessBuilder(commands)
                        .redirectError(ProcessBuilder.Redirect.INHERIT).redirectOutput(redirect).directory(rootDir).start();
                process.waitFor(1, TimeUnit.MINUTES);
            }
            System.out.println("done in " + (System.currentTimeMillis() - start) + "ms");
        }

        List<String> lines = new ArrayList<>();
        {
            start = System.currentTimeMillis();
            System.out.print("Reading log...");
            readLines(logFile, lines);
            //        if (!logFile.delete()) logFile.deleteOnExit();
            System.out.println("done in " + (System.currentTimeMillis() - start) + "ms");
        }


        Set<String> allRawAuthors = new TreeSet<>();
        List<Revision> revisions = new ArrayList<>();
        {
            start = System.currentTimeMillis();
            System.out.print("Parsing revisions...");
            Set<String> allParseduthors = new TreeSet<>();
            String tmpAuthor = null;
            String tmpDate;
            for (String line : lines) {
                if (line.startsWith("Author:")) {
                    tmpAuthor = line.substring(8);
                    String decodedAuthor = decode(parseNameAndSurname(tmpAuthor), aliases);
                    boolean isNew = allParseduthors.add(decodedAuthor);
                    String mainPart = tmpAuthor.toLowerCase() + " -> " + decodedAuthor;
                    if (!allRawAuthors.contains(mainPart)) {
                        allRawAuthors.add(mainPart + (isNew ? "" : " *"));
                    }
                }
                if (line.startsWith("Date:")) {
                    tmpDate = line.substring(5).trim();
                    Revision revision = new Revision(new Author(tmpAuthor, decode(parseNameAndSurname(tmpAuthor), aliases)), GIT_FORMAT.parse(tmpDate));
                    revisions.add(revision);
                }
            }
            Collections.sort(revisions);
            System.out.println("done in " + (System.currentTimeMillis() - start) + "ms");
        }

        try (FileWriter writer = new FileWriter(new File(baseDir, title + ".authors.txt"))) {
            for (String author : allRawAuthors) {
                writer.append(author).append(System.getProperty("line.separator"));
            }
        }

        List<AuthorRange> ranges = new ArrayList<>();
        {
            start = System.currentTimeMillis();
            System.out.print("Merging ranges...");
            AuthorRange currentRange = null;
            for (int i = 0; i < revisions.size(); i++) {
                Revision revision = revisions.get(i);
                if (revision.author.isBot()) {
                    continue;
                }

                if (currentRange == null) {
                    currentRange = new AuthorRange(revision.author, revision.date);
                    continue;
                }
                if (currentRange.author.compareTo(revision.author) == 0) {
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
            for (int i = 0; i < ranges.size(); i++) {
                AuthorRange base = ranges.get(i);
                for (int j = i + 1; j < ranges.size(); j++) {
                    AuthorRange range = ranges.get(j);
                    if (base.author.compareTo(range.author) == 0) {
                        base.merge(range);
                        ranges.remove(j);
                        j--;
                    }
                }
            }

            ranges.removeIf(authorRange -> authorRange.dates.size() < MIN_REVISIONS_NUMBER);
            ranges.removeIf(authorRange -> authorRange.getCommitsPerDay() < MIN_COMMITS_PER_DAY);
            System.out.println("done in " + (System.currentTimeMillis() - start) + "ms");
        }

        int counter = 1;
        for (AuthorRange range : ranges) {
            System.out.println(counter + ". " + range);
            counter++;
        }
        if (ranges.isEmpty()) {
            System.err.println("Ranges not found");
            return;
        }
        File picture = new File(title + ".png");

        {
            start = System.currentTimeMillis();
            System.out.print("Saving picture...");
            saveToImage(title, ranges, picture);
            System.out.println("done in " + (System.currentTimeMillis() - start) + "ms");
        }

        Desktop.getDesktop().open(picture);
    }

    private static String decode(String name, Properties aliases) {
        return aliases.getProperty(name, name);
    }
}
