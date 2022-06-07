package cositas.buenas.util;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

public class FileUtil {
    public static File mkDirs(String subfolder) throws IOException {
        File dir = new File(new File(System.getProperty("user.home")), subfolder);
        if (!dir.isDirectory() && !dir.mkdirs())
            throw new IOException("Cannot make directory " + dir.getAbsolutePath());
        return dir;
    }
}
