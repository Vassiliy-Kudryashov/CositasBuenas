/**
 * Created by user on 12/12/16.
 */
public class ConsoleTest {
    public static void main(String[] args) throws InterruptedException {
        String s = Thread.currentThread().getStackTrace()[1].toString();
        String s2 = "CodeGen.main(CodeGen.java:30)";
        Inner3 i3 = new Inner3();
        System.out.println(i3);
        for (int i = 0; i < 100000; i++) {
            System.out.println(s);
            System.out.println(s + "(");
            System.out.println(s + "()");
            System.out.println(s2);
            System.out.println(s2 + "(");
            System.out.println(s2 + "()");
            Thread.sleep(11);
        }
    }
    private interface Inner1 {

    }

    private static class Inner2  {}

    private static class Inner3 extends Inner2 implements Inner1{}
}
