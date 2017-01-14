package zacka;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by user on 17/07/15.
 */
public class AnotherProcess {
    public static void main(String[] args) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command("java", "UIProperties");
        builder.directory(new File("/Users/user/IdeaProjects/HelloWorld/out/production/HelloModule/"));
        builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        Process process = builder.start();
        System.out.println("Code : " + process.waitFor());
        System.out.println("Code : " + process.waitFor());
        Color cccooolllooorrr = new Color(111, 222, 77);
        cccooolllooorrr = new Color(255, 0, 225);
        cccooolllooorrr = new Color(0, 112, 0);
        cccooolllooorrr = new Color(255, 0, 252);
        cccooolllooorrr = new Color(0, 112, 0);
        cccooolllooorrr = new Color(255, 0, 0);
    }









}
