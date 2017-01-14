import java.io.*;

/**`
 * Cre`ated by user on 10/11/16.
 */
public class LogKiller {
    public static void main(String[] args) throws InterruptedException, IOException {
//        ProcessBuilder builder = new ProcessBuilder("bash", "rm *.log");
//        builder.directory(new File("/Users/user/IdeaProjects/HelloWorld/logs"));
//        builder.redirectError(ProcessBuilder.Redirect.INHERIT);
//        builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
//        Process process = builder.start();
//        process.waitFor();
        Process process = Runtime.getRuntime().exec(new String[]{"sh", "-c", "rm *.log"}, null, new File("/Users/user/IdeaProjects/HelloWorld/logs/"));
        InputStream inputStream = process.getErrorStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        for (String s = br.readLine(); s != null; s = br.readLine()) {
            System.out.println(s);
        }
        process.waitFor();
    }
}
