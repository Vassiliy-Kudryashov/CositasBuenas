package cositas.buenas.misc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by user on 17.03.14.
 */
public class CodeGen {

    public static final File BASE = new File("module/src/generated");
    private static Map<File, String> fileToMethod = new HashMap<File, String>();

    private int unused;

    public static void main(String[] args) {
        for (int i = 0; i < 5; i++) {
            File p = new File(BASE, "p" + i);
            p.mkdirs();
            for (int j = 0; j < 7; j++) {
                String name = "F" + j;
                File file = new File(p, name + ".java");
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(file);
                    StringBuilder sb = new StringBuilder("package " + p.getName() + ";\n");
                    sb.append("public class " + name + " {\n");
                    String methodName = "m" + name;
                    sb.append("  public static void " + methodName + "() {\n");
                    sb.append("//system.getErrors().addMessage(123)");
                    if (Math.random() > .75) sb.append(" Rule123");
                    sb.append(";\n");
                    for (Map.Entry<File, String> entry : fileToMethod.entrySet()) {
                        File f = entry.getKey();
                        String fName = f.getName().substring(0, f.getName().length() - 5);
                        sb.append("    ").append(f.getParentFile().getName() + "." + fName + "." + entry.getValue() + "();\n");
                    }
                    fileToMethod.put(file, methodName);


                    sb.append("  }\n");
                    sb.append("}");
                    String candidate = sb.toString();
                    /*candidate = */
                    candidate.replaceAll("<a href=\"#inspection/[\\w]+\">more\\.\\.\\.</a> \\(⌘F1\\)", "");

                    fos.write(candidate.getBytes());
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            //ignore
                        }
                    }
                }
            }
        }
    }
}
//       1         2         3         4         5         6         7        8         9         A         B         C12
/*
System


 */