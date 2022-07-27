package cositas.buenas.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtil {
    public static File mkDirs(String subfolder) throws IOException {
        File dir = new File(new File(System.getProperty("user.home")), subfolder);
        if (!dir.isDirectory() && !dir.mkdirs())
            throw new IOException("Cannot make directory " + dir.getAbsolutePath());
        return dir;
    }

    public static void save(CharSequence cs, File out) throws IOException{
        try(FileOutputStream fos = new FileOutputStream(out)) {
            fos.write(cs.toString().getBytes());
        }
    }
}
