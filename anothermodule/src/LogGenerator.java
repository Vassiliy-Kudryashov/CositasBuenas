import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by user on 09/11/16.
 */
public class LogGenerator {
    public static void main(String[] args) throws IOException {
        File BASE = new File("logs");
        BASE.mkdirs();
        for (int i = 0; i < 10; i++) {
            File f = new File(BASE, ""+i+".log");
            if (!f.isFile()) {
                new FileOutputStream(f).close();
            }
        }
    }
}
