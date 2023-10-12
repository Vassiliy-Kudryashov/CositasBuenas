package cositas.buenas.misc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.LockSupport;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KotovasiaGetter {
    static final List<String> SUFFIXES = Arrays.asList("au", "", "br", "ca", "cn2", "ae", "de", "hk", "blr", "id", "jp", "ma", "mx", "nj", "ng", "pk", "ph", "ru", "sa", "sg", "za", "se-rd", "tr", "uk");

    static void switchToRandomProxy() {
        String suffix = SUFFIXES.get(new Random().nextInt(SUFFIXES.size()));
        System.setProperty("java.net.useSystemProxies", "true");
        System.setProperty("http.proxyHost", "proxy" + suffix + ".huawei.com");
        System.setProperty("http.proxyPort", "8080");
        System.setProperty("https.proxyHost", "proxy" + suffix + ".huawei.com");
        System.setProperty("https.proxyPort", "8080");
    }

    public static void main(String[] args) throws IOException {
//        switchToRandomProxy();
//        System.setProperty("java.net.useSystemProxies", "true");
//        System.setProperty("http.proxyHost", "proxyuk.huawei.com");
//        System.setProperty("http.proxyPort", "8080");
//        System.setProperty("https.proxyHost", "proxyuk.huawei.com");
//        System.setProperty("https.proxyPort", "8080");
        File dir = new File(new File(new File(System.getProperty("user.home")),"Pictures"), "Cats");
        dir.mkdirs();
        String url = "https://dok-zlo.livejournal.com/tag/%D1%8E%D0%BC%D0%BE%D1%80";
//        url = "https://dok-zlo.livejournal.com/2022/08/19/";//initial
        Pattern postPattern = Pattern.compile("<a href=\"(https://dok-zlo\\.livejournal\\.com/\\d+\\.html)\" class=\"subj-link\"[^>]+>Котовасия</a>");
        Pattern imgPattern = Pattern.compile("(https://ic\\.pics\\.livejournal\\.com/dok_zlo/\\d+/\\d+/\\d+_[^.]+\\.jpg)");
        Pattern prevPattern = Pattern.compile("<a([^>]+)>Previous 20 Entries</a>");
        Pattern prevPattern2 = Pattern.compile("<a([^>]+?get=prev[^>]+)>");
        while (true) {
            String page = load(url);
            Matcher matcher = postPattern.matcher(page);
            while (matcher.find()) {
                String postUrl = matcher.group(1);
                String post = load(postUrl);
                System.out.println(postUrl);
                Matcher innerMatcher = imgPattern.matcher(post);
                while (innerMatcher.find()) {
                    String imgURL = innerMatcher.group(1).replace("_600", "_original");
                    int i = imgURL.lastIndexOf("/");
                    int j = imgURL.lastIndexOf("_");
                    if (i > 0 && j > i) {
                        File file = new File(dir, imgURL.substring(i + 1, j) + ".jpg");
                        if (file.isFile()) continue;
                        byte[] imageBytes = loadBytes(imgURL);
                        System.out.println(file.getAbsolutePath());
                        try (FileOutputStream fos = new FileOutputStream(file)) {
                            fos.write(imageBytes);
                        }
                    }
                }
            }
            Matcher prevMatcher = prevPattern.matcher(page);
            if (prevMatcher.find()) {
                url = prevMatcher.group(1);
                url = url.substring(url.indexOf("\"") + 1);
                url = url.substring(0, url.indexOf("\"")).replace("&amp;", "&");
                System.out.println(url);
            } else {
                prevMatcher = prevPattern2.matcher(page);
                if (prevMatcher.find()) {
                    url = prevMatcher.group(1);
                    url = url.substring(url.indexOf("\"") + 1);
                    url = url.substring(0, url.indexOf("\"")).replace("&amp;", "&");
                    System.out.println("* " + url);
                } else {
                    break;
                }
            }
        }
    }

    private static String load(String url) throws IOException {
        return new String(loadBytes(url), StandardCharsets.UTF_8);
    }

    private static byte[] loadBytes(String link) throws IOException {
        for (int retry = 0; retry < 4; retry++) {
            try {
                URL url = new URL(link);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Useg-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36");
                boolean redirect = false;

                int status = conn.getResponseCode();
                if (status != HttpURLConnection.HTTP_OK) {
                    if (status == HttpURLConnection.HTTP_MOVED_TEMP
                            || status == HttpURLConnection.HTTP_MOVED_PERM
                            || status == HttpURLConnection.HTTP_SEE_OTHER)
                        redirect = true;
                }
                if (redirect) {
                    // get redirect url from "location" header field
                    url = new URL(conn.getHeaderField("Location"));
                }
                LockSupport.parkUntil(System.currentTimeMillis() + 5000);
                try (InputStream inputStream = url.openStream()) {
                    byte[] buff = new byte[16384];
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    for (int len = inputStream.read(buff); len > 0; len = inputStream.read(buff)) {
                        baos.write(buff, 0, len);
                    }
                    return baos.toByteArray();
                }
            } catch (IOException e) {
                if (retry == 3) {
                    throw e;
                } else {
                    e.printStackTrace();
                    System.err.println("Retry " + link);
                    LockSupport.parkUntil(System.currentTimeMillis() + 5000);
                }
            }

        }
        throw new IOException("After retries still could not load " + link);
    }
}
