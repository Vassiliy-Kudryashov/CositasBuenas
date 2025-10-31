package cositas.buenas.misc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileDuplicateDetector {
    private static final Map<Integer, List<String>> stats = new HashMap<>();

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("You may specify scan folder or drive as program argument, total scan will be started now.");
            File[] roots = File.listRoots();
            if (roots !=null) {
                for (File root : roots) {
                    processRoot(root.toPath(), false);
                }
            }
        } else {
            processRoot(Paths.get(args[0]), true);
        }
    }

    private static void processRoot(Path root, boolean clearStats) throws IOException{
        int minFileSize = 1024;
        System.out.println("Start searching for file duplicates with size >= " +minFileSize + " bytes in " + root);
        if (clearStats) {
            stats.clear();
        }
        AtomicInteger counter = new AtomicInteger();
        Files.walk(root).forEach(path -> {
                    try {
                        if (Files.isRegularFile(path) && Files.size(path) >= minFileSize) {
                            if (path.toString().endsWith(".zip") || path.toString().endsWith(".jar")) {
                                walkThroughZip(path, minFileSize, counter, stats);
                            } else {
                                int hash = getHash(path);
                                if (hash != 0) {
                                    counter.incrementAndGet();
                                    List<String> list = stats.getOrDefault(hash, new ArrayList<>());
                                    list.add(path.toAbsolutePath().toString());
                                    stats.put(hash, list);
                                }
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
        System.out.println(counter.get() + " files were collected for analysis");
        System.out.println("Stats size: " + stats.size());
        for (Map.Entry<Integer, List<String>> entry : stats.entrySet()) {
            List<String> list = entry.getValue();
            if (list.size() > 1) {
                long size = 0;
                System.out.println("***");
                for (String path : list) {
                    if (size == 0) {
                        size = new File(path).length();
                    }
                    System.out.println(path + " (" + size + " bytes)");
                }
            }
        }
    }

    private static void walkThroughZip(Path zipPath, int minFileSize, AtomicInteger counter, Map<Integer, List<String>> stats) throws IOException {
        System.err.println(zipPath);
        try (ZipFile zipFile = new ZipFile(zipPath.toFile())) {
            zipFile.stream().forEach(zipEntry -> {
                if (!zipEntry.isDirectory() && zipEntry.getSize() >= minFileSize) {
                    int hash = getHash(zipFile, zipEntry);
                    if (hash != 0) {
                        counter.incrementAndGet();
                        List<String> list = stats.getOrDefault(hash, new ArrayList<>());
                        list.add(zipPath.toAbsolutePath().toString() + zipEntry);
                        stats.put(hash, list);
                    }
                }
            });
        }
    }

    private static int getHash(Path path) {
        try(InputStream inputStream = Files.newInputStream(path)) {
            return getHash(inputStream);
        } catch (Exception e) {
            return 0;
        }
    }

    private static int getHash(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[16384];
        int result = 1;
        for (int len = inputStream.read(buffer); len > 0; len = inputStream.read(buffer)) {
            for (int i = 0; i < len; i++) {
                byte b = buffer[i];
                result = 31 * result + b;
            }
        }
        return result;
    }

    private static int getHash(ZipFile zipFile, ZipEntry zipEntry) {
        try (InputStream inputStream = zipFile.getInputStream(zipEntry)) {
            return getHash(inputStream);
        } catch (IOException ignored) {
            return 0;
        }
    }
}
