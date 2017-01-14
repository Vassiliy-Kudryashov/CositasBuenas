package zacka.packa;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by user on 30.07.14.
 */
public class HelloWorld  {
    public static void main(String[] args) throws InterruptedException {
        ArrayList<String> testList = new ArrayList<>();
        testList.add("One");
        testList.add(0, "Two");
        testList.add(testList.size(), "Three");
        System.out.println(Arrays.toString(testList.toArray()));
        long time = System.currentTimeMillis();
        System.out.println(time -1);
//        System.out.println(System.currentTimeMillis()-1, 2);
        System.out.println(System.currentTimeMillis()-1);
        System.out.println(System.currentTimeMillis()-1);
        System.out.println(time = System.currentTimeMillis()+1);
        System.out.println(System.currentTimeMillis()+2);
        System.out.println(System.currentTimeMillis());
        while(true) {
            System.out.println(System.currentTimeMillis());
            Thread.sleep(1111);
            if (Math.random()<.1) break;
        }
        System.out.println("abc\u0000def");
        Thread.sleep(11111);
        time--;
        System.out.println(time);
//        List aList = new ArrayList<String>();
    }













































}
